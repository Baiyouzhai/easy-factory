package com.byz.factory.iot.model;

import com.alibaba.fastjson2.JSON;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/**
 * JPA 属性转换器 — List&lt;String&gt; ↔ JSON 文本。
 *
 * @author easy-factory
 */
@Converter
public class JsonListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        return JSON.toJSONString(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return JSON.parseArray(dbData, String.class);
    }

}
