package com.byz.data;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import com.byz.util.CollectionUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public interface IExpand {

    //region 原生部分

    /**
     * 获取原生属性
     *
     * @return 原生属性
     */
    @JSONField(serialize = false)
    default Map<String, Object> getNativeProperties() {
        return JSONObject.from(this, JSONWriter.Feature.FieldBased);
    }

    /**
     * 设置原生属性
     *
     * @param properties 属性
     */
    default void setNativeProperties(Map<String, Object> properties) {
        if (null == properties) return;
        properties.forEach(this::setNativeProperty);
    }

    /**
     * 获取原生属性键
     *
     * @return 原生属性键
     */
    @JSONField(serialize = false)
    default Set<String> getNativePropertyKeys() {
        return getNativeProperties().keySet();
    }

    /**
     * 判断是否原生属性
     *
     * @param key 键
     * @return 是否原生属性
     */
    default boolean isNativeProperty(String key) {
        return getNativePropertyKeys().contains(key);
    }

    /**
     * 获取原生属性
     *
     * @param key 键
     * @return 值
     */
    default Object getNativeProperty(String key) {
        return getNativeProperties().get(key);
    }

    /**
     * 设置原生属性
     *
     * @param key   键
     * @param value 值
     * @return 旧的值
     */
    default Object setNativeProperty(String key, Object value) {
        try {
            Field field = this.getClass().getDeclaredField(key);
            field.setAccessible(true);
            Object val = field.get(this);
            field.set(this, value);
            return val;
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region 扩展部分

    /**
     * 获取扩展属性
     *
     * @return 扩展属性
     */
    @JSONField(serialize = false)
    Map<String, Object> getExpandProperties();

    /**
     * 设置扩展属性
     *
     * @param properties 属性
     */
    default void setExpandProperties(Map<String, Object> properties) {
        Map<String, Object> expand = getExpandProperties();
        if (null != expand) {
            Set<String> nativePropertyKeys = getNativePropertyKeys();
            properties.forEach((key, value) -> {
                if (!nativePropertyKeys.contains(key)) {
                    expand.put(key, value);
                }
            });
        }
    }

    /**
     * 获取扩展属性键
     *
     * @return 扩展属性键
     */
    @JSONField(serialize = false)
    default Set<String> getExpandPropertyKeys() {
        Map<String, Object> expand = getExpandProperties();
        return null == expand ? new HashSet<>() : expand.keySet();
    }

    /**
     * 判断是否扩展属性
     *
     * @param key 键
     * @return 是否扩展属性
     */
    default boolean isExpandProperty(String key) {
        if (isNativeProperty(key)) return false;
        Map<String, Object> expand = getExpandProperties();
        return null != expand && expand.containsKey(key);
    }

    /**
     * 获取扩展属性
     *
     * @param key 键
     * @return 值
     */
    default Object getExpandProperty(String key) {
        Map<String, Object> expand = getExpandProperties();
        return null == expand ? null : expand.get(key);
    }

    /**
     * 设置扩展属性
     *
     * @param key   键
     * @param value 值
     * @return 旧的值
     */
    default Object setExpandProperty(String key, Object value) {
        Map<String, Object> expand = getExpandProperties();
        if (null == expand) return value;
        return isNativeProperty(key) ? value : expand.put(key, value);
    }

    /**
     * 移除扩展属性
     *
     * @param key 键
     * @return 值
     */
    default Object removeExpandProperty(String key) {
        Map<String, Object> expand = getExpandProperties();
        return null == expand ? null : expand.remove(key);
    }
    //endregion

    //region 混合部分

    /**
     * 获取属性
     *
     * @return 属性
     */
    @JSONField(serialize = false)
    default Map<String, Object> getProperties() {
        Map<String, Object> nativeP = getNativeProperties(); // 原生属性
        Map<String, Object> expand = getExpandProperties(); // 扩展属性
        if (null == expand) return nativeP;
        Map<String, Object> properties = new HashMap<>(nativeP);
        CollectionUtil.MapRemove(properties, nativeP, expand); // 防止死循环
        Map<String, Object> expandMap = new HashMap<>(expand);
        CollectionUtil.MapRemove(expandMap, nativeP);
        CollectionUtil.MapRemove(expandMap, expand);
        expandMap.forEach((key, value) -> {
            Object nativeV = nativeP.get(key);
            if (null == nativeV) {
                properties.put(key, value);
            } else {
                if (null != value) { // 扩展属性覆盖原生属性
                    properties.put(key, value);
                }
            }
        });
        return properties;
    }

    /**
     * 设置属性
     *
     * @param properties 属性
     */
    default void setProperties(Map<String, Object> properties) {
        if (null == properties) return;
        Set<String> nativeKeys = getNativePropertyKeys();
        properties.forEach((key, value) -> {
            if (nativeKeys.contains(key)) setNativeProperty(key, value);
            else setExpandProperty(key, value);
        });
    }

    /**
     * 获取属性键
     *
     * @return 属性键
     */
    @JSONField(serialize = false)
    default Set<String> getPropertyKeys() {
        return getProperties().keySet();
    }

    /**
     * 获取属性
     *
     * @param key 键
     * @return 值
     */
    default Object getProperty(String key) {
        Map<String, Object> properties = getNativeProperties();
        if (properties.containsKey(key)) return properties.get(key);
        return getExpandProperty(key);
    }

    /**
     * 设置属性
     *
     * @param key   键
     * @param value 值
     * @return 旧的值
     */
    default Object setProperty(String key, Object value) {
        return isNativeProperty(key)
                ? setNativeProperty(key, value)
                : setExpandProperty(key, value);
    }

    /**
     * 移除属性
     *
     * @param key 键
     * @return 值
     */
    default Object removeProperty(String key) {
        return isNativeProperty(key)
                ? setNativeProperty(key, null)
                : removeExpandProperty(key);
    }
    //endregion

}
