package com.byz.factory.scm.service;

import com.byz.factory.scm.PurchaseOrderStatus;
import com.byz.factory.scm.SupplierStatus;
import com.byz.factory.scm.model.InboundPlan;
import com.byz.factory.scm.model.PurchaseOrder;
import com.byz.factory.scm.model.PurchaseOrderItem;
import com.byz.factory.scm.model.Supplier;
import com.byz.factory.scm.repository.InboundPlanRepository;
import com.byz.factory.scm.repository.PurchaseOrderRepository;
import com.byz.factory.scm.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * SCM 供应链管理服务实现。
 * <p>
 * 使用 Spring 事务管理，确保供应商注册、采购订单创建、来料计划通知
 * 等操作的原子性。
 *
 * @author 苏政
 */
@Service
@Transactional
public class ScmServiceImpl implements ScmService {

    private final SupplierRepository supplierRepo;
    private final PurchaseOrderRepository poRepo;
    private final InboundPlanRepository inboundPlanRepo;

    public ScmServiceImpl(SupplierRepository supplierRepo,
                          PurchaseOrderRepository poRepo,
                          InboundPlanRepository inboundPlanRepo) {
        this.supplierRepo = supplierRepo;
        this.poRepo = poRepo;
        this.inboundPlanRepo = inboundPlanRepo;
    }

    // ==================== 供应商管理 ====================

    @Override
    public Supplier registerSupplier(String code, String name, String category) {
        if (supplierRepo.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("供应商编码已存在: " + code);
        }
        Supplier supplier = new Supplier(code, name, category);
        return supplierRepo.save(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier findSupplier(String code) {
        return supplierRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("供应商不存在: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getQualifiedSuppliers() {
        return supplierRepo.findByQualification("QUALIFIED");
    }

    @Override
    public void qualifySupplier(String code) {
        Supplier supplier = findSupplier(code);
        supplier.qualify();
        supplierRepo.save(supplier);
    }

    @Override
    public void disqualifySupplier(String code) {
        Supplier supplier = findSupplier(code);
        supplier.disqualify();
        supplierRepo.save(supplier);
    }

    @Override
    public void deactivateSupplier(String code) {
        Supplier supplier = findSupplier(code);
        supplier.deactivate();
        supplierRepo.save(supplier);
    }

    @Override
    public void reactivateSupplier(String code) {
        Supplier supplier = findSupplier(code);
        supplier.reactivate();
        supplierRepo.save(supplier);
    }

    // ==================== 采购订单管理 ====================

    @Override
    public PurchaseOrder createPurchaseOrder(String poNo, String supplierCode) {
        // 验证供应商存在且处于可合作状态
        Supplier supplier = findSupplier(supplierCode);
        if (supplier.getStatus() == SupplierStatus.BLACKLISTED) {
            throw new IllegalStateException("已拉黑的供应商不能创建采购订单: " + supplierCode);
        }

        if (poRepo.findByPoNo(poNo).isPresent()) {
            throw new IllegalArgumentException("采购单号已存在: " + poNo);
        }

        PurchaseOrder po = new PurchaseOrder(poNo, supplierCode);
        return poRepo.save(po);
    }

    @Override
    public void approvePurchaseOrder(String poNo, String approvedBy) {
        PurchaseOrder po = findPurchaseOrder(poNo);
        po.approve(approvedBy);
        poRepo.save(po);
    }

    @Override
    public void sendPurchaseOrder(String poNo) {
        PurchaseOrder po = findPurchaseOrder(poNo);
        po.send();
        poRepo.save(po);
    }

    @Override
    public void receivePurchaseOrder(String poNo, String materialCode, BigDecimal receivedQty) {
        PurchaseOrder po = findPurchaseOrder(poNo);

        // 查找对应物料行
        Optional<PurchaseOrderItem> itemOpt = po.getItems().stream()
                .filter(i -> i.getMaterialCode().equals(materialCode))
                .findFirst();

        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    "采购订单 " + poNo + " 中不存在物料: " + materialCode);
        }

        PurchaseOrderItem item = itemOpt.get();
        item.receive(receivedQty);

        // 自动推进状态
        if (po.getStatus() == PurchaseOrderStatus.SENT) {
            po.startReceiving();
        }

        // 全部收齐则完成
        if (po.isFullyReceived()) {
            po.complete();
        }

        poRepo.save(po);
    }

    @Override
    public void cancelPurchaseOrder(String poNo) {
        PurchaseOrder po = findPurchaseOrder(poNo);
        po.cancel();
        poRepo.save(po);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrder findPurchaseOrder(String poNo) {
        return poRepo.findByPoNo(poNo)
                .orElseThrow(() -> new IllegalArgumentException("采购订单不存在: " + poNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getPurchaseOrdersBySupplier(String supplierCode) {
        return poRepo.findBySupplierCode(supplierCode);
    }

    // ==================== 来料计划管理（ScmService 扩展） ====================

    /**
     * 创建来料计划——基于已审批的采购订单。
     * <p>
     * 典型调用链：PO 审批通过 → 创建 InboundPlan → WMS 订阅 → 生成收货单。
     *
     * @param planCode     来料计划编号
     * @param poNo         关联采购订单号
     * @param expectedDate 预计到货日期
     * @return 创建的来料计划
     */
    public InboundPlan createInboundPlan(String planCode, String poNo,
                                         java.time.LocalDate expectedDate) {
        PurchaseOrder po = findPurchaseOrder(poNo);

        if (po.getStatus() != PurchaseOrderStatus.APPROVED
                && po.getStatus() != PurchaseOrderStatus.SENT) {
            throw new IllegalStateException(
                    "采购订单状态不正确，无法创建来料计划: " + po.getStatus());
        }

        if (inboundPlanRepo.findByCode(planCode).isPresent()) {
            throw new IllegalArgumentException("来料计划编号已存在: " + planCode);
        }

        InboundPlan plan = new InboundPlan(planCode, poNo, po.getSupplierCode());
        plan.setExpectedDate(expectedDate);
        return inboundPlanRepo.save(plan);
    }

    /**
     * 通知仓库准备收货（CREATED → NOTIFIED）。
     */
    public void notifyWarehouse(String planCode) {
        InboundPlan plan = inboundPlanRepo.findByCode(planCode)
                .orElseThrow(() -> new IllegalArgumentException("来料计划不存在: " + planCode));
        plan.notifyWarehouse();
        inboundPlanRepo.save(plan);
    }

    /**
     * 按采购订单号查询来料计划。
     */
    @Transactional(readOnly = true)
    public Optional<InboundPlan> findInboundPlanByPoNo(String poNo) {
        return inboundPlanRepo.findByPoNo(poNo);
    }

    /**
     * 获取供应商的所有来料计划。
     */
    @Transactional(readOnly = true)
    public List<InboundPlan> getInboundPlansBySupplier(String supplierCode) {
        return inboundPlanRepo.findBySupplierCode(supplierCode);
    }

}
