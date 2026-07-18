package com.byz.factory.scm;

import com.byz.factory.batch.PurchaseOrderStatus;
import com.byz.factory.batch.SupplierStatus;
import com.byz.factory.scm.model.PurchaseOrder;
import com.byz.factory.scm.model.PurchaseOrderItem;
import com.byz.factory.scm.model.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SCM 模块模型构造测试")
class ScmModuleTest {

    // ==================== Supplier ====================

    @Test
    @DisplayName("Supplier 创建 — 字段正确初始化")
    void supplier_creation_shouldSetFields() {
        Supplier supplier = new Supplier("SUP-001", "华东原料供应", "RAW_MATERIAL");

        assertEquals("SUP-001", supplier.getCode());
        assertEquals("华东原料供应", supplier.getName());
        assertEquals("RAW_MATERIAL", supplier.getCategory());
        assertEquals("UNDER_REVIEW", supplier.getQualification());
        assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        assertEquals(0, supplier.getLeadTimeDays());
        assertEquals(0.0, supplier.getOnTimeRate());
        assertEquals(0.0, supplier.getQualityRate());
        assertNotNull(supplier.getCreatedAt());
    }

    @Test
    @DisplayName("Supplier — 资质审核通过")
    void supplier_qualify_shouldSetQualified() {
        Supplier supplier = new Supplier("SUP-002", "南方包材", "PACKAGING");
        supplier.qualify();

        assertEquals("QUALIFIED", supplier.getQualification());
    }

    @Test
    @DisplayName("Supplier — 取消资质")
    void supplier_disqualify_shouldSetDisqualified() {
        Supplier supplier = new Supplier("SUP-003", "设备供应商", "EQUIPMENT");
        supplier.qualify();
        supplier.disqualify();

        assertEquals("DISQUALIFIED", supplier.getQualification());
    }

    @Test
    @DisplayName("Supplier — 暂停与恢复合作")
    void supplier_deactivateAndReactivate() {
        Supplier supplier = new Supplier("SUP-004", "检测服务商", "SERVICE");

        supplier.deactivate();
        assertEquals(SupplierStatus.INACTIVE, supplier.getStatus());

        supplier.reactivate();
        assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
    }

    @Test
    @DisplayName("Supplier — 永久拉黑")
    void supplier_blacklist() {
        Supplier supplier = new Supplier("SUP-005", "不合格供应商", "RAW_MATERIAL");

        supplier.blacklist();
        assertEquals(SupplierStatus.BLACKLISTED, supplier.getStatus());
    }

    // ==================== PurchaseOrder ====================

    @Test
    @DisplayName("PurchaseOrder 创建 — 初始状态 DRAFT")
    void purchaseOrder_creation_shouldSetDraftStatus() {
        PurchaseOrder po = new PurchaseOrder("PO-001", "SUP-001");

        assertEquals("PO-001", po.getPoNo());
        assertEquals("SUP-001", po.getSupplierCode());
        assertEquals(PurchaseOrderStatus.DRAFT, po.getStatus());
        assertNotNull(po.getItems());
        assertTrue(po.getItems().isEmpty());
    }

    @Test
    @DisplayName("PurchaseOrder — 可添加采购明细行")
    void purchaseOrder_addItem_shouldAddToItems() {
        PurchaseOrder po = new PurchaseOrder("PO-002", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));

        assertEquals(1, po.getItems().size());
        assertEquals("MAT-001", po.getItems().get(0).getMaterialCode());
        assertEquals(new BigDecimal("100"), po.getItems().get(0).getQuantity());
    }

    // ==================== PurchaseOrderItem ====================

    @Test
    @DisplayName("PurchaseOrderItem 创建 — 已收数量初始为零")
    void purchaseOrderItem_creation_shouldSetZeroReceived() {
        PurchaseOrderItem item = new PurchaseOrderItem("MAT-001", new BigDecimal("100"));

        assertEquals("MAT-001", item.getMaterialCode());
        assertEquals(new BigDecimal("100"), item.getQuantity());
        assertEquals(BigDecimal.ZERO, item.getReceivedQty());
    }

}
