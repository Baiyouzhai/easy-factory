package com.byz.factory.process;

import com.byz.data.IExpand;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.shared.Dict.SourceGroup;

import java.util.*;
import java.util.stream.Stream;

/**
 * 资源标签过滤器 — 从动作的资源需求中按标签提取子系统关注的信息。
 * <p>
 * 资源通过 {@code IExpand.set("subsystem.property", value)} 打标签，
 * 各子系统通过 TagFilter 过滤自己关注的标签前缀。
 * <p>
 * 用法：
 * <pre>{@code
 * // Equip 提取设备相关资源
 * var equipRes = TagFilter.of(action.requireResources())
 *     .hasTag("equip.required")
 *     .group(SourceGroup.Machine)
 *     .list();
 *
 * // QMS 提取检验相关标签值
 * Map<String, Object> qmsTags = TagFilter.of(action.requireResources())
 *     .hasTagPrefix("qms.")
 *     .tagValues();
 * }</pre>
 *
 * @author 苏政
 */
public class TagFilter {

    private final IResourceItem[] resources;
    private String requiredTag;
    private String tagPrefix;
    private SourceGroup groupFilter;

    private TagFilter(IResourceItem[] resources) {
        this.resources = resources != null ? resources : new IResourceItem[0];
    }

    public static TagFilter of(IResourceItem[] resources) {
        return new TagFilter(resources);
    }

    /** 要求资源必须有指定标签 */
    public TagFilter hasTag(String tagKey) {
        this.requiredTag = tagKey;
        return this;
    }

    /** 要求资源必须有指定标签前缀 */
    public TagFilter hasTagPrefix(String prefix) {
        this.tagPrefix = prefix;
        return this;
    }

    /** 过滤资源分组 */
    public TagFilter group(SourceGroup group) {
        this.groupFilter = group;
        return this;
    }

    /** 返回过滤后的资源列表 */
    public List<IResourceItem> list() {
        return stream().toList();
    }

    /** 返回过滤后的资源数量 */
    public int count() {
        return (int) stream().count();
    }

    /** 是否存在满足条件的资源 */
    public boolean exists() {
        return stream().findAny().isPresent();
    }

    /** 提取所有匹配的标签键值对 */
    public Map<String, Object> tagValues() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (IResourceItem r : resources) {
            if (!matches(r)) continue;
            if (tagPrefix != null) {
                for (Map.Entry<String, Object> e : expandAll(r).entrySet()) {
                    if (e.getKey().startsWith(tagPrefix)) {
                        result.put(e.getKey(), e.getValue());
                    }
                }
            } else if (requiredTag != null) {
                Object v = expandValue(r, requiredTag);
                if (v != null) result.put(requiredTag, v);
            }
        }
        return result;
    }

    /** 获取第一个匹配的资源（常用于单资源查询） */
    public Optional<IResourceItem> first() {
        return stream().findFirst();
    }

    /** 获取匹配资源的名称列表 */
    public List<String> names() {
        return stream().map(IResourceItem::getName).toList();
    }

    private Stream<IResourceItem> stream() {
        return Arrays.stream(resources)
            .filter(Objects::nonNull)
            .filter(this::matches);
    }

    private boolean matches(IResourceItem r) {
        if (groupFilter != null && r.getGroup() != groupFilter) return false;
        IExpand exp = (r instanceof IExpand e) ? e : null;
        if (requiredTag != null && (exp == null || exp.getExpandProperty(requiredTag) == null)) return false;
        if (tagPrefix != null && exp != null) {
            return exp.getExpandPropertyKeys().stream().anyMatch(k -> k.startsWith(tagPrefix));
        }
        if (tagPrefix != null && exp == null) return false;
        return true;
    }

    /** 从资源获取扩展属性，无 IExpand 返回 null */
    private static Object expandValue(IResourceItem r, String key) {
        return (r instanceof IExpand exp) ? exp.getExpandProperty(key) : null;
    }

    /** 从资源获取全部扩展属性，无 IExpand 返回空 Map */
    private static Map<String, Object> expandAll(IResourceItem r) {
        return (r instanceof IExpand exp) ? exp.getExpandProperties() : Collections.emptyMap();
    }

    /** 从资源获取扩展属性键集合 */
    private static Set<String> expandKeys(IResourceItem r) {
        return (r instanceof IExpand exp) ? exp.getExpandPropertyKeys() : Collections.emptySet();
    }
}
