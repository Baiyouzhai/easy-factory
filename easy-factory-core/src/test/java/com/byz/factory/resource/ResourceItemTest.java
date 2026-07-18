package com.byz.factory.resource;

import com.byz.factory.exception.ResourceException;
import com.byz.factory.shared.Dict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourceItem 资源项测试")
class ResourceItemTest {

    @Test
    @DisplayName("add — 补充数量")
    void add_increasesNumber() {
        ResourceItem item = new ResourceItem("原料", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        item.add(new BigDecimal("5"));
        assertEquals(new BigDecimal("15"), item.getNumber());
    }

    @Test
    @DisplayName("use — 消耗数量")
    void use_decreasesNumber() {
        ResourceItem item = new ResourceItem("原料", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        item.use(new BigDecimal("3"));
        assertEquals(new BigDecimal("7"), item.getNumber());
    }

    @Test
    @DisplayName("use — 负余额保护")
    void use_throwsOnNegative() {
        ResourceItem item = new ResourceItem("原料", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ONE);
        assertThrows(ResourceException.class, () -> item.use(new BigDecimal("10")));
    }

    @Test
    @DisplayName("copy — 深拷贝独立对象")
    void copy_createsIndependentObject() {
        ResourceItem item = new ResourceItem("原料", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        IResourceItem copy = item.copy();

        assertNotSame(item, copy);
        assertEquals(item.getName(), copy.getName());
        assertEquals(item.getNumber(), copy.getNumber());

        // 修改原始不影响副本
        item.use(new BigDecimal("5"));
        assertEquals(new BigDecimal("5"), item.getNumber());
        assertEquals(new BigDecimal("10"), copy.getNumber());
    }

    @Test
    @DisplayName("isEmpty — 零数量为空")
    void isEmpty_zeroIsEmpty() {
        ResourceItem item = new ResourceItem("空", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ZERO);
        assertTrue(item.isEmpty());
    }

    @Test
    @DisplayName("isEmpty — 正数量非空")
    void isEmpty_positiveIsNotEmpty() {
        ResourceItem item = new ResourceItem("非空", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ONE);
        assertFalse(item.isEmpty());
    }

    @Test
    @DisplayName("require — 默认为 false（接口约定）")
    void require_defaultFalse() {
        ResourceItem item = new ResourceItem("可选", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ONE);
        assertFalse(item.require());
    }

    @Test
    @DisplayName("put — 与 add 相同语义")
    void put_sameAsAdd() {
        ResourceItem item = new ResourceItem("原料", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        item.put(new BigDecimal("10"));
        assertEquals(new BigDecimal("20"), item.getNumber());
    }

    @Test
    @DisplayName("EMPTY_ARRAY 常量正确")
    void emptyArray_isEmpty() {
        assertEquals(0, ResourceItem.EMPTY_ARRAY.length);
    }
}
