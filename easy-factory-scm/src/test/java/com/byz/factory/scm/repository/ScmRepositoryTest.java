package com.byz.factory.scm.repository;

import com.byz.factory.scm.PurchaseOrderStatus;
import com.byz.factory.scm.SupplierStatus;
import com.byz.factory.scm.model.PurchaseOrder;
import com.byz.factory.scm.model.PurchaseOrderItem;
import com.byz.factory.scm.model.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("SCM Repository 集成测试")
class ScmRepositoryTest {

    @Autowired
    private SupplierRepository supplierRepo;

    @Autowired
    private PurchaseOrderRepository poRepo;

    // ==================== SupplierRepository ====================

    @Test
    @DisplayName("Supplier — 保存并查回")
    void supplier_saveAndFind() {
        Supplier supplier = new Supplier("SUP-001", "华东原料供应", "RAW_MATERIAL");
        Supplier saved = supplierRepo.save(supplier);

        assertNotNull(saved.getId(), "save 后应自动生成 id");

        Optional<Supplier> found = supplierRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("SUP-001", found.get().getCode());
        assertEquals("华东原料供应", found.get().getName());
    }

    @Test
    @DisplayName("Supplier — findByCode 按编码查询")
    void supplier_findByCode() {
        supplierRepo.save(new Supplier("SUP-002", "南方包材", "PACKAGING"));

        Optional<Supplier> found = supplierRepo.findByCode("SUP-002");
        assertTrue(found.isPresent());
        assertEquals("南方包材", found.get().getName());
    }

    @Test
    @DisplayName("Supplier — findByCategory 按类别查询")
    void supplier_findByCategory() {
        supplierRepo.save(new Supplier("SUP-003", "原料A", "RAW_MATERIAL"));
        supplierRepo.save(new Supplier("SUP-004", "原料B", "RAW_MATERIAL"));
        supplierRepo.save(new Supplier("SUP-005", "设备商", "EQUIPMENT"));

        List<Supplier> raw = supplierRepo.findByCategory("RAW_MATERIAL");
        assertEquals(2, raw.size());

        List<Supplier> equip = supplierRepo.findByCategory("EQUIPMENT");
        assertEquals(1, equip.size());
    }

    @Test
    @DisplayName("Supplier — findByStatus 按运营状态查询")
    void supplier_findByStatus() {
        Supplier s1 = new Supplier("SUP-006", "活跃商", "RAW_MATERIAL");
        Supplier s2 = new Supplier("SUP-007", "暂停商", "RAW_MATERIAL");
        s2.deactivate();
        supplierRepo.save(s1);
        supplierRepo.save(s2);

        List<Supplier> active = supplierRepo.findByStatus(SupplierStatus.ACTIVE);
        assertEquals(1, active.size());
        assertEquals("SUP-006", active.get(0).getCode());

        List<Supplier> inactive = supplierRepo.findByStatus(SupplierStatus.INACTIVE);
        assertEquals(1, inactive.size());
        assertEquals("SUP-007", inactive.get(0).getCode());
    }

    @Test
    @DisplayName("Supplier — findByQualification 按资质查询")
    void supplier_findByQualification() {
        Supplier s1 = new Supplier("SUP-008", "待审商", "SERVICE");
        Supplier s2 = new Supplier("SUP-009", "合格商", "RAW_MATERIAL");
        s2.qualify();
        supplierRepo.save(s1);
        supplierRepo.save(s2);

        List<Supplier> reviewing = supplierRepo.findByQualification("UNDER_REVIEW");
        assertEquals(1, reviewing.size());

        List<Supplier> qualified = supplierRepo.findByQualification("QUALIFIED");
        assertEquals(1, qualified.size());
    }

    @Test
    @DisplayName("Supplier — 双维度组合查询")
    void supplier_findByQualificationAndStatus() {
        Supplier s1 = new Supplier("SUP-010", "合格活跃", "RAW_MATERIAL");
        s1.qualify();
        Supplier s2 = new Supplier("SUP-011", "合格暂停", "RAW_MATERIAL");
        s2.qualify();
        s2.deactivate();
        supplierRepo.save(s1);
        supplierRepo.save(s2);

        List<Supplier> result = supplierRepo.findByQualificationAndStatus("QUALIFIED", SupplierStatus.ACTIVE);
        assertEquals(1, result.size());
        assertEquals("SUP-010", result.get(0).getCode());
    }

    @Test
    @DisplayName("Supplier — 枚举持久化为字符串")
    void supplier_enumPersistence() {
        Supplier supplier = new Supplier("SUP-012", "测试商", "RAW_MATERIAL");
        supplier.deactivate();
        Supplier saved = supplierRepo.saveAndFlush(supplier);

        Supplier found = supplierRepo.findByCode("SUP-012").orElseThrow();
        assertEquals(SupplierStatus.INACTIVE, found.getStatus(),
                "运营状态枚举应持久化为字符串并正确还原");
        assertEquals("UNDER_REVIEW", found.getQualification(),
                "资质状态字符串应正确持久化");
    }

    @Test
    @DisplayName("Supplier — createdAt/updatedAt 自动填充")
    void supplier_auditTimestamps() {
        Supplier supplier = new Supplier("SUP-013", "审计测试", "SERVICE");
        Supplier saved = supplierRepo.save(supplier);

        assertNotNull(saved.getCreatedAt(), "createdAt 应自动填充");
        assertNotNull(saved.getUpdatedAt(), "updatedAt 应自动填充");
    }

    @Test
    @DisplayName("Supplier — leadTime/onTimeRate/qualityRate 持久化")
    void supplier_performanceFields() {
        Supplier supplier = new Supplier("SUP-014", "高绩效", "RAW_MATERIAL");
        supplier.setLeadTimeDays(14);
        supplier.setOnTimeRate(98.5);
        supplier.setQualityRate(99.2);
        Supplier saved = supplierRepo.saveAndFlush(supplier);

        Supplier found = supplierRepo.findByCode("SUP-014").orElseThrow();
        assertEquals(14, found.getLeadTimeDays());
        assertEquals(98.5, found.getOnTimeRate());
        assertEquals(99.2, found.getQualityRate());
    }

    // ==================== PurchaseOrderRepository ====================

    @Test
    @DisplayName("PurchaseOrder — 保存并查回，初始状态 DRAFT")
    void purchaseOrder_saveAndFind() {
        PurchaseOrder po = new PurchaseOrder("PO-001", "SUP-001");
        PurchaseOrder saved = poRepo.save(po);

        assertNotNull(saved.getId());
        assertEquals(PurchaseOrderStatus.DRAFT, saved.getStatus());
        assertEquals("PO-001", saved.getPoNo());
    }

    @Test
    @DisplayName("PurchaseOrder — findByPoNo 按单号查询")
    void purchaseOrder_findByPoNo() {
        poRepo.save(new PurchaseOrder("PO-002", "SUP-001"));

        Optional<PurchaseOrder> found = poRepo.findByPoNo("PO-002");
        assertTrue(found.isPresent());
        assertEquals("SUP-001", found.get().getSupplierCode());
    }

    @Test
    @DisplayName("PurchaseOrder — findBySupplierCode 按供应商查询")
    void purchaseOrder_findBySupplierCode() {
        poRepo.save(new PurchaseOrder("PO-003", "SUP-001"));
        poRepo.save(new PurchaseOrder("PO-004", "SUP-001"));
        poRepo.save(new PurchaseOrder("PO-005", "SUP-002"));

        List<PurchaseOrder> forSup1 = poRepo.findBySupplierCode("SUP-001");
        assertEquals(2, forSup1.size());

        List<PurchaseOrder> forSup2 = poRepo.findBySupplierCode("SUP-002");
        assertEquals(1, forSup2.size());
    }

    @Test
    @DisplayName("PurchaseOrder — 状态转换持久化")
    void purchaseOrder_statusTransitionPersistence() {
        PurchaseOrder po = new PurchaseOrder("PO-006", "SUP-001");
        po.approve("张三");
        po.send();
        PurchaseOrder saved = poRepo.saveAndFlush(po);

        PurchaseOrder found = poRepo.findById(saved.getId()).orElseThrow();
        assertEquals(PurchaseOrderStatus.SENT, found.getStatus(),
                "SENT 状态应正确持久化");
        assertEquals("张三", found.getApprovedBy());
    }

    @Test
    @DisplayName("PurchaseOrder — 所有状态均可持久化（完整生命周期）")
    void purchaseOrder_fullLifecyclePersistence() {
        PurchaseOrder po = new PurchaseOrder("PO-007", "SUP-001");

        // DRAFT
        PurchaseOrder draft = poRepo.saveAndFlush(po);
        assertEquals(PurchaseOrderStatus.DRAFT, poRepo.findById(draft.getId()).orElseThrow().getStatus());

        // APPROVED
        po.approve("李四");
        poRepo.saveAndFlush(po);
        assertEquals(PurchaseOrderStatus.APPROVED, poRepo.findById(draft.getId()).orElseThrow().getStatus());

        // SENT
        po.send();
        poRepo.saveAndFlush(po);
        assertEquals(PurchaseOrderStatus.SENT, poRepo.findById(draft.getId()).orElseThrow().getStatus());

        // COMPLETED
        po.complete();
        poRepo.saveAndFlush(po);
        assertEquals(PurchaseOrderStatus.COMPLETED, poRepo.findById(draft.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("PurchaseOrder — CANCELLED 状态持久化")
    void purchaseOrder_cancelledPersistence() {
        PurchaseOrder po = new PurchaseOrder("PO-008", "SUP-001");
        po.cancel();
        PurchaseOrder saved = poRepo.saveAndFlush(po);

        PurchaseOrder found = poRepo.findById(saved.getId()).orElseThrow();
        assertEquals(PurchaseOrderStatus.CANCELLED, found.getStatus());
    }

    @Test
    @DisplayName("PurchaseOrder — 单条明细级联保存并查回")
    void purchaseOrder_cascadeSaveSingleItem() {
        PurchaseOrder po = new PurchaseOrder("PO-010", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));
        PurchaseOrder saved = poRepo.saveAndFlush(po);

        PurchaseOrder found = poRepo.findById(saved.getId()).orElseThrow();
        assertEquals(1, found.getItems().size());
        assertEquals("MAT-001", found.getItems().get(0).getMaterialCode());
        assertEquals(new BigDecimal("100"), found.getItems().get(0).getQuantity());
        assertNotNull(found.getItems().get(0).getId(), "明细项应有自增 id");
    }

    @Test
    @DisplayName("PurchaseOrder — 多条明细级联保存并查回")
    void purchaseOrder_cascadeSaveMultipleItems() {
        PurchaseOrder po = new PurchaseOrder("PO-011", "SUP-001");
        PurchaseOrderItem item1 = po.addItem("MAT-001", new BigDecimal("100"));
        item1.setUnitPrice(new BigDecimal("25.50"));
        PurchaseOrderItem item2 = po.addItem("MAT-002", new BigDecimal("200"));
        item2.setUnitPrice(new BigDecimal("10.00"));
        PurchaseOrder saved = poRepo.saveAndFlush(po);

        PurchaseOrder found = poRepo.findById(saved.getId()).orElseThrow();
        assertEquals(2, found.getItems().size());

        // 验证 BigDecimal 精度
        PurchaseOrderItem foundItem1 = found.getItems().get(0);
        assertEquals(0, new BigDecimal("100").compareTo(foundItem1.getQuantity()));
        assertEquals(0, new BigDecimal("25.50").compareTo(foundItem1.getUnitPrice()));
    }

    @Test
    @DisplayName("PurchaseOrder — 明细收货数据持久化")
    void purchaseOrder_itemReceivePersistence() {
        PurchaseOrder po = new PurchaseOrder("PO-012", "SUP-001");
        PurchaseOrderItem item = po.addItem("MAT-001", new BigDecimal("100"));
        item.receive(new BigDecimal("30"));
        item.receive(new BigDecimal("20"));
        PurchaseOrder saved = poRepo.saveAndFlush(po);

        PurchaseOrder found = poRepo.findById(saved.getId()).orElseThrow();
        PurchaseOrderItem foundItem = found.getItems().get(0);
        assertEquals(0, new BigDecimal("50").compareTo(foundItem.getReceivedQty()),
                "已收数量应正确持久化");
    }

    @Test
    @DisplayName("PurchaseOrder — 删除 PO 时级联删除明细")
    void purchaseOrder_cascadeDeleteItems() {
        PurchaseOrder po = new PurchaseOrder("PO-013", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));
        po.addItem("MAT-002", new BigDecimal("200"));
        PurchaseOrder saved = poRepo.saveAndFlush(po);

        poRepo.deleteById(saved.getId());
        poRepo.flush();

        Optional<PurchaseOrder> found = poRepo.findById(saved.getId());
        assertFalse(found.isPresent(), "删除 PO 后不应存在");
    }

    @Test
    @DisplayName("PurchaseOrder — expectedDelivery 日期持久化")
    void purchaseOrder_datePersistence() {
        PurchaseOrder po = new PurchaseOrder("PO-014", "SUP-001");
        LocalDate delivery = LocalDate.of(2026, 8, 15);
        po.setExpectedDelivery(delivery);
        PurchaseOrder saved = poRepo.saveAndFlush(po);

        PurchaseOrder found = poRepo.findById(saved.getId()).orElseThrow();
        assertEquals(delivery, found.getExpectedDelivery());
    }

    @Test
    @DisplayName("PurchaseOrder — totalAmount 不持久化（计算字段）")
    void purchaseOrder_totalAmountNotPersisted() {
        PurchaseOrder po = new PurchaseOrder("PO-015", "SUP-001");
        PurchaseOrderItem item = po.addItem("MAT-001", new BigDecimal("100"));
        item.setUnitPrice(new BigDecimal("25.50"));
        poRepo.saveAndFlush(po);

        PurchaseOrder found = poRepo.findById(po.getId()).orElseThrow();
        // totalAmount 是计算值，不依赖持久化，从明细动态计算
        assertEquals(0, new BigDecimal("2550.00").compareTo(found.totalAmount()));
    }

    // ==================== 综合场景 ====================

    @Test
    @DisplayName("综合 — Supplier 从注册到拉黑全流程")
    void integration_supplierFullLifecycle() {
        // 注册
        Supplier supplier = new Supplier("SUP-100", "综合测试商", "RAW_MATERIAL");
        supplierRepo.save(supplier);

        // 资质审核
        Supplier found = supplierRepo.findByCode("SUP-100").orElseThrow();
        found.qualify();
        supplierRepo.save(found);
        assertEquals("QUALIFIED", supplierRepo.findByCode("SUP-100").orElseThrow().getQualification());

        // 暂停合作
        found = supplierRepo.findByCode("SUP-100").orElseThrow();
        found.deactivate();
        supplierRepo.save(found);
        assertEquals(SupplierStatus.INACTIVE, supplierRepo.findByCode("SUP-100").orElseThrow().getStatus());

        // 恢复合作
        found = supplierRepo.findByCode("SUP-100").orElseThrow();
        found.reactivate();
        supplierRepo.save(found);
        assertEquals(SupplierStatus.ACTIVE, supplierRepo.findByCode("SUP-100").orElseThrow().getStatus());
    }

    @Test
    @DisplayName("综合 — PO 从创建到完成，含收货记录")
    void integration_poFullLifecycle() {
        // 创建
        PurchaseOrder po = new PurchaseOrder("PO-100", "SUP-100");
        po.addItem("MAT-001", new BigDecimal("100"));
        po.addItem("MAT-002", new BigDecimal("50"));
        poRepo.save(po);

        // 审批
        PurchaseOrder found = poRepo.findByPoNo("PO-100").orElseThrow();
        found.approve("张三");
        poRepo.save(found);

        // 发送
        found = poRepo.findByPoNo("PO-100").orElseThrow();
        found.send();
        poRepo.save(found);

        // 收货
        found = poRepo.findByPoNo("PO-100").orElseThrow();
        found.getItems().get(0).receive(new BigDecimal("40"));
        found.startReceiving();
        poRepo.save(found);

        // 验证收货数据
        found = poRepo.findByPoNo("PO-100").orElseThrow();
        assertEquals(PurchaseOrderStatus.RECEIVING, found.getStatus());
        assertEquals(0, new BigDecimal("40").compareTo(found.getItems().get(0).getReceivedQty()));
        assertEquals(0, new BigDecimal("0").compareTo(found.getItems().get(1).getReceivedQty()));

        // 完成
        found.getItems().get(0).receive(new BigDecimal("60"));
        found.getItems().get(1).receive(new BigDecimal("50"));
        found.complete();
        poRepo.save(found);

        found = poRepo.findByPoNo("PO-100").orElseThrow();
        assertEquals(PurchaseOrderStatus.COMPLETED, found.getStatus());
        assertTrue(found.isFullyReceived());
    }

}
