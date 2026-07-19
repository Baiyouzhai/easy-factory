package com.byz.factory.wms.model;

import com.byz.factory.batch.IReceipt;
import com.byz.factory.batch.MaterialStatus;
import com.byz.factory.batch.ReceiptStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 收货单 — 继承 BaseLifecycleEntity 获得状态机（PENDING→PARTIAL→COMPLETED→CLOSED），
 * 实现 IReceipt 供 SCM/QMS 等模块编译期引用。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   wms.receiptNo      — 收货单号
 *   wms.sourceType      — 来源类型
 *   wms.referenceNo     — 来源单号
 *   wms.supplierCode    — 供应商编码
 *   wms.receivedBy      — 收货人
 *   wms.receivedAt      — 收货时间
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Receipt extends BaseLifecycleEntity<ReceiptStatus> implements IReceipt {

    /** 来源类型（PURCHASE_ORDER / RETURN / TRANSFER） */
    private String sourceType;

    /** 来源单号 */
    private String referenceNo;

    /** 供应商编码 */
    private String supplierCode;

    /** 收货人 */
    private String receivedBy;

    /** 收货时间 */
    private Instant receivedAt;

    /** 收货明细 */
    private List<ReceiptItem> items;

    /**
     * @param receiptNo   收货单号
     * @param referenceNo 来源单号
     * @param supplierCode 供应商编码
     */
    public Receipt(String receiptNo, String referenceNo, String supplierCode) {
        super(receiptNo, "收货-" + receiptNo, ReceiptStatus.PENDING);
        this.referenceNo = referenceNo;
        this.supplierCode = supplierCode;
        this.items = new ArrayList<>();
    }

    // ==================== IReceipt 实现 ====================

    @Override
    public String getReceiptNo() {
        return getCode();
    }

    // ==================== 状态转换便捷方法 ====================

    /** 开始收货（PENDING → PARTIAL） */
    public void receive(String receivedBy) {
        transition(ReceiptStatus.PARTIAL);
        this.receivedBy = receivedBy;
        this.receivedAt = Instant.now();
    }

    /** 标记完成——全部收货完毕（PARTIAL → COMPLETED） */
    public void complete() {
        transition(ReceiptStatus.COMPLETED);
    }

    /** 关闭收货单（COMPLETED → CLOSED） */
    public void close() {
        transition(ReceiptStatus.CLOSED);
    }

    // ==================== 业务方法 ====================

    /**
     * 添加收货明细行（来料默认进入待检状态）。
     *
     * @param materialCode 物料编码
     * @param batchNo      供应商批号
     * @param receivedQty  实收数量
     * @param orderedQty   订单数量
     * @return 创建的明细行
     */
    public ReceiptItem addItem(String materialCode, String batchNo, BigDecimal receivedQty, BigDecimal orderedQty) {
        ReceiptItem item = new ReceiptItem(materialCode, batchNo, receivedQty, orderedQty, null, MaterialStatus.QUARANTINE);
        this.items.add(item);
        markUpdated();
        return item;
    }

    /**
     * 验收物料行——合格放行到指定库位。
     *
     * @param materialCode 物料编码
     * @param locationCode 上架库位
     */
    public void acceptItem(String materialCode, String locationCode) {
        for (ReceiptItem item : items) {
            if (item.getMaterialCode().equals(materialCode) && item.getStatus() == MaterialStatus.QUARANTINE) {
                item.setLocationCode(locationCode);
                item.setStatus(MaterialStatus.RELEASED);
            }
        }
        markUpdated();
    }

    /**
     * 拒收物料行——检验不合格。
     *
     * @param materialCode 物料编码
     */
    public void rejectItem(String materialCode) {
        for (ReceiptItem item : items) {
            if (item.getMaterialCode().equals(materialCode) && item.getStatus() == MaterialStatus.QUARANTINE) {
                item.setStatus(MaterialStatus.REJECTED);
            }
        }
        markUpdated();
    }

    /**
     * 判断所有物料是否已验收完毕（全部 RELEASED 或 REJECTED）。
     */
    public boolean isFullyProcessed() {
        return items.stream().allMatch(i -> i.getStatus() == MaterialStatus.RELEASED || i.getStatus() == MaterialStatus.REJECTED);
    }

    /**
     * 收货明细 — 实现 IReceipt.IReceiptItem 供跨模块引用。
     */
    @Data
    public static class ReceiptItem implements IReceipt.IReceiptItem {

        private String materialCode;
        private String batchNo;
        private BigDecimal receivedQty;
        private BigDecimal orderedQty;
        private String locationCode;
        private MaterialStatus status;

        public ReceiptItem() {}

        public ReceiptItem(String materialCode, String batchNo, BigDecimal receivedQty,
                           BigDecimal orderedQty, String locationCode, MaterialStatus status) {
            this.materialCode = materialCode;
            this.batchNo = batchNo;
            this.receivedQty = receivedQty;
            this.orderedQty = orderedQty;
            this.locationCode = locationCode;
            this.status = status;
        }

    }

}
