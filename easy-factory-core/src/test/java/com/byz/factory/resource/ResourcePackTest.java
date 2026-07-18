package com.byz.factory.resource;

import com.byz.factory.exception.ResourceException;
import com.byz.factory.shared.Dict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourcePack 资源包测试")
class ResourcePackTest {

    private ResourcePack pack;

    @BeforeEach
    void setUp() {
        pack = new ResourcePack();
    }

    @Test
    @DisplayName("merge — 新增资源")
    void merge_addsNewResource() {
        ResourceItem item = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        pack.merge(item);
        assertEquals(1, pack.getResources().length);
    }

    @Test
    @DisplayName("merge — 重复资源抛异常")
    void merge_duplicateThrows() {
        ResourceItem item = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        pack.merge(item);
        assertThrows(ResourceException.class, () -> pack.merge(item));
    }

    @Test
    @DisplayName("compress — 合并相同 group+name 资源")
    void compress_mergesSameGroupAndName() {
        ResourceItem a1 = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, new BigDecimal("10"));
        ResourceItem a2 = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, new BigDecimal("20"));
        pack.merge(a1);
        pack.merge(a2);

        pack.compress();
        assertEquals(1, pack.getResources().length);
        assertEquals(new BigDecimal("30"), pack.getResources()[0].getNumber());
    }

    @Test
    @DisplayName("compress — 不同资源保持独立")
    void compress_keepsDifferentResources() {
        ResourceItem a = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        ResourceItem b = new ResourceItem("原料B", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        pack.merge(a);
        pack.merge(b);

        pack.compress();
        assertEquals(2, pack.getResources().length);
    }

    @Test
    @DisplayName("copy — 深拷贝独立对象")
    void copy_createsIndependentPack() {
        ResourceItem item = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        pack.merge(item);

        ResourcePack copy = pack.copy();
        assertNotSame(pack, copy);
        assertEquals(1, copy.getResources().length);

        // 修改原始不影响副本
        pack.merge(new ResourceItem("原料B", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ONE));
        assertEquals(2, pack.getResources().length);
        assertEquals(1, copy.getResources().length);
    }

    @Test
    @DisplayName("isEmpty — 新包为空")
    void isEmpty_newPackIsEmpty() {
        assertTrue(pack.isEmpty());
    }

    @Test
    @DisplayName("isEmpty(false) — 有资源但resourceCheck=false 仍为false")
    void isEmpty_noResourceCheck() {
        pack.merge(new ResourceItem("X", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ONE));
        assertFalse(pack.isEmpty());  // resourceCheck=false
    }

    @Test
    @DisplayName("requireResources — 只返回 require=true 的资源")
    void requireResources_filtersRequired() {
        ResourceItem item = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN) {
            @Override public boolean require() { return true; }
        };
        pack.merge(item);
        assertEquals(1, pack.requireResources().length);
    }
}
