package com.byz.data;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Type;
import java.util.Map;

public interface IBaseData extends Cloneable {

    /**
     * 复制属性
     *
     * @param source 源
     * @param ignoreProperties 忽略属性
     * @return this
     */
    default IBaseData copyProperties(Object source, String... ignoreProperties) {
        if (null == source) return this;
        BeanUtils.copyProperties(source, this, ignoreProperties);
        return this;
    }

    /**
     * 转换成Map
     *
     * @return Map
     */
    default Map<String, Object> toMap() {
        return JSONObject.from(this).to(TypeReference.mapType(String.class, Object.class));
    }

    /**
     * 转换成json字符串
     *
     * @return json字符串
     */
    default String toJsonString() {
        return JSONObject.toJSONString(this);
    }

    /**
     * json序列化方式克隆
     *
     * @return 克隆
     */
    default IBaseData jsonClone() {
        String jsonString = toJsonString();
        return JSONObject.parseObject(jsonString, (Type) getClass());
    }

    /**
     * 克隆, 使用 {@link IBaseData#jsonClone()}
     *
     * @return 克隆
     */
    default IBaseData clone() {
        return jsonClone();
    }

}
