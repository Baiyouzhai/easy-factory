package com.byz.data;

import java.util.Properties;

public interface IDataExpand {

    /**
     * 获取扩展属性
     *
     * @return 扩展属性
     */
    default Properties getProperties() {
        return null;
    }

    /**
     * 获取属性
     *
     * @param key 键
     * @return 值
     */
    default String getProperty(String key) {
        Properties properties = getProperties();
        if (null == properties) return null;
        return properties.getProperty(key);
    }

    /**
     * 设置属性
     *
     * @param key 键
     * @param value 值
     * @return 旧的值
     */
    default String setProperty(String key, String value) {
        Properties properties = getProperties();
        if (null == properties) return null;
        Object obj = properties.setProperty(key, value);
        return null == obj ? null : obj.toString();
    }

    /**
     * 移除属性
     *
     * @param key 键
     * @return 值
     */
    default String removeProperty(String key) {
        Properties properties = getProperties();
        if (null == properties) return null;
        Object obj = properties.remove(key);
        return null == obj ? null : obj.toString();
    }

}
