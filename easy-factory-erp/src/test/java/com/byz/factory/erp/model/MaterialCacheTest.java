package com.byz.factory.erp.model;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.UOM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaterialCache 物料缓存测试")
class MaterialCacheTest {

    @Test
    @DisplayName("构造 — materialCode/description/unit 正确赋值")
    void constructor_shouldSetRequiredFields() {
        MaterialCache cache = new MaterialCache("MAT-001", "阿莫西林原料药", "KG");

        assertEquals("MAT-001", cache.getCode(), "code 应为 materialCode");
        assertEquals("阿莫西林原料药", cache.getName(), "name 应为 description");
        assertEquals("阿莫西林原料药", cache.getDescription());
        assertEquals("KG", cache.getUnit());
    }

    @Test
    @DisplayName("默认值 — materialType=ROH, batchManaged=false, shelfLifeDays=0")
    void defaults_shouldBeSensible() {
        MaterialCache cache = new MaterialCache("M1", "物料", "PCS");

        assertEquals(ErpMaterialType.ROH, cache.getMaterialType());
        assertFalse(cache.isBatchManaged());
        assertEquals(0, cache.getShelfLifeDays());
    }

    @Test
    @DisplayName("自动审计 — createdAt/updatedAt 在构造时自动设置")
    void audit_shouldAutoSetTimestamps() {
        MaterialCache cache = new MaterialCache("M1", "物料", "PCS");

        assertNotNull(cache.getCreatedAt(), "createdAt 应在构造时自动设置");
        assertNotNull(cache.getUpdatedAt(), "updatedAt 应在构造时自动设置");
        assertEquals(cache.getCreatedAt(), cache.getUpdatedAt(), "初始时 createdAt == updatedAt");
    }

    @Test
    @DisplayName("toResourceItem — 映射 name/group/type/number")
    void toResourceItem_shouldMapCoreFields() {
        MaterialCache cache = new MaterialCache("MAT-001", "阿莫西林原料药", "KG");

        IResourceItem item = cache.toResourceItem();

        assertEquals("MAT-001", item.getName(), "物料编码映射为 name");
        assertEquals(Dict.SourceGroup.Material, item.getGroup(), "分组固定为 Material");
        assertNotNull(item.getType(), "type 不应为 null");
        assertNotNull(item.getNumber(), "number 不应为 null");
    }

    @Test
    @DisplayName("toResourceItem — ROH 映射为 RawMaterial")
    void toResourceItem_ROH_shouldMapToRawMaterial() {
        MaterialCache cache = new MaterialCache("M1", "原料", "KG");
        cache.setMaterialType(ErpMaterialType.ROH);

        IResourceItem item = cache.toResourceItem();

        assertEquals(Dict.SourceType.RawMaterial, item.getType());
    }

    @Test
    @DisplayName("toResourceItem — HALB 映射为 WIP")
    void toResourceItem_HALB_shouldMapToWIP() {
        MaterialCache cache = new MaterialCache("M2", "中间体", "KG");
        cache.setMaterialType(ErpMaterialType.HALB);

        IResourceItem item = cache.toResourceItem();

        assertEquals(Dict.SourceType.WIP, item.getType());
    }

    @Test
    @DisplayName("toResourceItem — FERT 映射为 FinishedGood")
    void toResourceItem_FERT_shouldMapToFinishedGood() {
        MaterialCache cache = new MaterialCache("M3", "成品药", "BOX");
        cache.setMaterialType(ErpMaterialType.FERT);

        IResourceItem item = cache.toResourceItem();

        assertEquals(Dict.SourceType.FinishedGood, item.getType());
    }

    @Test
    @DisplayName("mapUom — KG 映射正确")
    void mapUom_shouldMapKG() {
        MaterialCache cache = new MaterialCache("M1", "物料", "KG");
        assertEquals(UOM.KG, cache.mapUom());
    }

    @Test
    @DisplayName("mapUom — PCS/PC/EA 都映射为 PCS")
    void mapUom_shouldMapPieceUnits() {
        assertEquals(UOM.PCS, new MaterialCache("M1", "个", "PCS").mapUom());
        assertEquals(UOM.PCS, new MaterialCache("M2", "个", "PC").mapUom());
        assertEquals(UOM.PCS, new MaterialCache("M3", "个", "EA").mapUom());
    }

    @Test
    @DisplayName("mapUom — 未知单位映射为 NONE")
    void mapUom_unknown_shouldReturnNone() {
        assertEquals(UOM.NONE, new MaterialCache("M1", "物料", "UNKNOWN").mapUom());
        assertEquals(UOM.NONE, new MaterialCache("M2", "物料", null).mapUom());
    }

    @Test
    @DisplayName("mapUom — 大小写不敏感")
    void mapUom_shouldBeCaseInsensitive() {
        assertEquals(UOM.KG, new MaterialCache("M1", "kg", "kg").mapUom());
        assertEquals(UOM.L, new MaterialCache("M2", "l", "l").mapUom());
    }

    @Test
    @DisplayName("sourceSystem / lastSyncTime — 可设可读")
    void erpFields_shouldBeReadWrite() {
        MaterialCache cache = new MaterialCache("M1", "物料", "PCS");
        cache.setSourceSystem("SAP");
        cache.setGhsClass("TOXIC");
        cache.setBatchManaged(true);
        cache.setShelfLifeDays(365);

        assertEquals("SAP", cache.getSourceSystem());
        assertEquals("TOXIC", cache.getGhsClass());
        assertTrue(cache.isBatchManaged());
        assertEquals(365, cache.getShelfLifeDays());
    }

}
