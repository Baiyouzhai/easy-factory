package com.byz.factory.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BillOfMaterial 物料清单测试")
class BillOfMaterialTest {

    @Test
    @DisplayName("构造器 — productCode 正确设置")
    void constructor_setsProductCode() {
        BillOfMaterial bom = new BillOfMaterial("PROD-001");
        assertEquals("PROD-001", bom.getProductCode());
        assertTrue(bom.getMaterials().isEmpty());
    }

    @Test
    @DisplayName("无参构造 — 初始化空materials列表")
    void defaultConstructor_emptyMaterials() {
        BillOfMaterial bom = new BillOfMaterial();
        assertNull(bom.getProductCode());
        assertTrue(bom.getMaterials().isEmpty());
    }

    @Test
    @DisplayName("materials — 添加和读取行项")
    void materials_addAndRead() {
        BillOfMaterial bom = new BillOfMaterial("PROD-001");
        BillOfMaterial.MaterialLineItem item = new BillOfMaterial.MaterialLineItem(
            "MAT-001", "原料A", new BigDecimal("10.5"), "kg", 1);
        bom.getMaterials().add(item);

        assertEquals(1, bom.getMaterials().size());
        BillOfMaterial.MaterialLineItem read = bom.getMaterials().get(0);
        assertEquals("MAT-001", read.materialCode());
        assertEquals("原料A", read.materialName());
        assertEquals(0, new BigDecimal("10.5").compareTo(read.quantity()));
        assertEquals("kg", read.unit());
        assertEquals(1, read.lineNumber());
    }

    @Test
    @DisplayName("MaterialLineItem — record 各字段正确")
    void materialLineItem_recordFields() {
        BillOfMaterial.MaterialLineItem item = new BillOfMaterial.MaterialLineItem(
            "M1", "物料1", BigDecimal.ONE, "pcs", 10);

        assertEquals("M1", item.materialCode());
        assertEquals("物料1", item.materialName());
        assertEquals(0, BigDecimal.ONE.compareTo(item.quantity()));
        assertEquals("pcs", item.unit());
        assertEquals(10, item.lineNumber());
    }

    @Test
    @DisplayName("setProductCode — 更新产品编码")
    void setProductCode_updates() {
        BillOfMaterial bom = new BillOfMaterial();
        bom.setProductCode("NEW-001");
        assertEquals("NEW-001", bom.getProductCode());
    }

    @Test
    @DisplayName("setMaterials — 替换整个物料列表")
    void setMaterials_replaces() {
        BillOfMaterial bom = new BillOfMaterial("P1");
        bom.getMaterials().add(new BillOfMaterial.MaterialLineItem(
            "M1", "物料1", BigDecimal.ONE, "pcs", 1));

        bom.setMaterials(new java.util.ArrayList<>());
        assertTrue(bom.getMaterials().isEmpty());
    }

}
