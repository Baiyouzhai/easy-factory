package com.byz.factory.iot.model;

import com.alibaba.fastjson2.JSON;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

/**
 * JPA 属性转换器 — Map&lt;String, Object&gt; ↔ JSON 文本。
 *
 * @author easy-factory
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) {
            return null;
        }
        return JSON.toJSONString(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return JSON.parseObject(dbData, Map.class);
    }

}
