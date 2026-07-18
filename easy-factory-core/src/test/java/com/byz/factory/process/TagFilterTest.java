package com.byz.factory.process;

import com.byz.data.IExpand;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.Dict.SourceGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TagFilter 标签过滤测试")
class TagFilterTest {

    private TaggedResource machine, personnel, method;

    @BeforeEach
    void setUp() {
        machine = new TaggedResource("压片机 PT-001", SourceGroup.Machine);
        machine.tag("equip.required", true);
        machine.tag("equip.tooling", "Φ12mm");

        personnel = new TaggedResource("操作工", SourceGroup.Personnel);
        personnel.tag("personnel.skill", "压片操作");
        personnel.tag("personnel.count", 2);

        method = new TaggedResource("片重检测", SourceGroup.Method);
        method.tag("qms.method", "称重法");
        method.tag("qms.interval", "15min");
    }

    @Test
    @DisplayName("按标签过滤 — equip.required")
    void filterByExactTag() {
        var result = TagFilter.of(new ResourceItem[]{machine, personnel, method})
            .hasTag("equip.required")
            .list();
        assertEquals(1, result.size());
        assertEquals("压片机 PT-001", result.get(0).getName());
    }

    @Test
    @DisplayName("按标签前缀过滤 — qms.*")
    void filterByPrefix() {
        var result = TagFilter.of(new ResourceItem[]{machine, personnel, method})
            .hasTagPrefix("qms.")
            .list();
        assertEquals(1, result.size());
        assertEquals("片重检测", result.get(0).getName());
    }

    @Test
    @DisplayName("按分组+标签组合过滤")
    void filterByGroupAndTag() {
        var result = TagFilter.of(new ResourceItem[]{machine, personnel, method})
            .hasTag("equip.required")
            .group(SourceGroup.Machine)
            .list();
        assertEquals(1, result.size());

        // 非Machine分组的equip标签应被过滤掉
        var empty = TagFilter.of(new ResourceItem[]{personnel, method})
            .hasTag("equip.required")
            .group(SourceGroup.Machine)
            .list();
        assertTrue(empty.isEmpty());
    }

    @Test
    @DisplayName("提取标签键值对")
    void tagValues() {
        var tags = TagFilter.of(new ResourceItem[]{machine})
            .hasTagPrefix("equip.")
            .tagValues();
        assertTrue(tags.containsKey("equip.required"));
        assertTrue(tags.containsKey("equip.tooling"));
    }

    @Test
    @DisplayName("first — 获取第一个匹配资源")
    void first() {
        var r = TagFilter.of(new ResourceItem[]{machine, personnel, method})
            .hasTagPrefix("personnel.")
            .first();
        assertTrue(r.isPresent());
        assertEquals("操作工", r.get().getName());
    }

    @Test
    @DisplayName("exists — 判断是否存在")
    void exists() {
        assertTrue(TagFilter.of(new ResourceItem[]{machine}).hasTag("equip.required").exists());
        assertFalse(TagFilter.of(new ResourceItem[]{machine}).hasTag("equip.nonexist").exists());
    }

    /** 带 IExpand 标签能力的资源 */
    static class TaggedResource extends ResourceItem implements IExpand {
        private final java.util.Map<String, Object> expand = new java.util.LinkedHashMap<>();
        TaggedResource(String name, SourceGroup group) {
            super(name, group, Dict.SourceType.Other, BigDecimal.ONE);
        }
        void tag(String key, Object value) { expand.put(key, value); }
        // 覆盖 IExpand 相关方法供 TagFilter 使用
        public java.util.Map<String, Object> getExpandProperties() { return expand; }
        public Object getExpandProperty(String key) { return expand.get(key); }
        public java.util.Set<String> getExpandPropertyKeys() { return expand.keySet(); }
    }
}
