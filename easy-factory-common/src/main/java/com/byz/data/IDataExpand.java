package com.byz.data;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Type;
import java.util.Map;

public interface IDataExpand extends IData, IExpand {

    @Override
    default IData copyProperties(Object source, String... ignoreProperties) {
        if (source instanceof IDataExpand expand) {
            BeanUtils.copyProperties(source, this, ignoreProperties);
            Map<String, Object> map = expand.getExpandProperties();
            this.setExpandProperties(map);
            return this;
        }
        return IData.super.copyProperties(source, ignoreProperties);
    }

    /**
     * 将对象转换成Map
     *
     * @param ignoreExpand 是否忽略扩展属性
     * @return Map
     */
    default Map<String, Object> toMap(boolean ignoreExpand) {
        return ignoreExpand
                ? IData.super.toMap()
                : getProperties();
    }

    /**
     * {@inheritDoc} 调用 {@link #toMap(boolean)}<br/>
     * toMap(ignoreExpand=false)
     */
    @Override
    default Map<String, Object> toMap() {
        return toMap(false);
    }

    /**
     * 将对象转换成json字符串
     *
     * @param ignoreExpand 是否忽略扩展属性
     * @return json字符串
     */
    default String toJsonString(boolean ignoreExpand) {
        if (ignoreExpand) return IData.super.toJsonString();
        Map<String, Object> properties = getProperties();
        if (properties instanceof JSONObject json) return json.toJSONString();
        return JSONObject.toJSONString(properties);
    }

    /**
     * {@inheritDoc} 调用 {@link #toJsonString(boolean)}<br/>
     * toJsonString(ignoreExpand=false)
     */
    @Override
    default String toJsonString() {
        return toJsonString(false);
    }

    /**
     * json序列化方式克隆
     *
     * @param ignoreExpand 是否忽略扩展属性
     * @return 克隆
     */
    default <T extends IDataExpand> T jsonClone(boolean ignoreExpand) {
        String jsonString = toJsonString(true);
        T expand = JSONObject.parseObject(jsonString, (Type) getClass());
        if (ignoreExpand) return expand;
        Map<String, Object> properties = getExpandProperties();
        expand.setExpandProperties(properties);
        return expand;
    }

    /**
     * {@inheritDoc} 调用 {@link #jsonClone(boolean)}<br/>
     * jsonClone(ignoreExpand=false)
     */
    @Override
    default <T extends IData> T jsonClone() {
        return jsonClone(false);
    }

}
