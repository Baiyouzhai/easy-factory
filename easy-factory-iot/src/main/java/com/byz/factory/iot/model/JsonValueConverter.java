package com.byz.factory.iot.model;

import com.alibaba.fastjson2.JSON;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/**
 * JPA 属性转换器 — Object ↔ JSON 文本。
 * <p>
 * 将采集的原始值（可能是 Number/String/Boolean/null）序列化为 JSON 文本存储。
 *
 * @author easy-factory
 */
@Converter
public class JsonValueConverter implements AttributeConverter<Object, String> {

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) {
            return null;
        }
        // 数字类型直接转字符串保持精度
        if (attribute instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        return JSON.toJSONString(attribute);
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        // 尝试解析为数字
        try {
            return new BigDecimal(dbData);
        } catch (NumberFormatException e) {
            // 非数字，尝试 JSON 解析
            try {
                return JSON.parse(dbData);
            } catch (Exception ex) {
                return dbData; // 返回原始字符串
            }
        }
    }

}
