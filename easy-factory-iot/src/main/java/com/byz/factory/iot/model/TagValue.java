package com.byz.factory.iot.model;

import com.byz.factory.iot.ITagValue;
import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 采集标签值 — 从设备读取的实时数据点，实现 ITagValue。
 * <p>
 * 封装原始值 → 工程值的换算链：scaledValue = rawValue × multiplier + offset。
 * 数据质量参照 OPC UA StatusCode 质量位分类。
 *
 * @author 苏政
 */
@Entity
@Table(name = "iot_tag_value")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TagValue extends BaseEntity implements ITagValue {

    @Column(name = "equipment_code", nullable = false, length = 100)
    private String equipmentCode;

    @Column(name = "tag_name", nullable = false, length = 200)
    private String tagName;

    @Convert(converter = JsonValueConverter.class)
    @Column(name = "raw_value", columnDefinition = "text")
    private Object value;

    @Column(name = "scaled_value", precision = 20, scale = 6)
    private BigDecimal scaledValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TagQuality quality;

    @Column(name = "ts_timestamp")
    private Instant timestamp;

    @Column(name = "server_timestamp")
    private Instant serverTimestamp;

    @Column(name = "batch_id", length = 100)
    private String batchId;

    @Column(precision = 20, scale = 6)
    private BigDecimal multiplier;

    @Column(name = "tag_offset", precision = 20, scale = 6)
    private BigDecimal offset;

    /**
     * @param equipmentCode 设备编码
     * @param tagName       标签名
     * @param value         原始值
     */
    public TagValue(String equipmentCode, String tagName, Object value) {
        super(equipmentCode + "." + tagName, tagName);
        this.equipmentCode = equipmentCode;
        this.tagName = tagName;
        this.value = value;
        this.quality = TagQuality.GOOD;
        this.timestamp = Instant.now();
        this.serverTimestamp = Instant.now();
        this.multiplier = BigDecimal.ONE;
        this.offset = BigDecimal.ZERO;
        this.scaledValue = value instanceof BigDecimal bd ? bd : BigDecimal.ZERO;
    }

    /** 创建 GOOD 质量的标签值 */
    public static TagValue of(String equipmentCode, String tagName, BigDecimal scaledValue) {
        TagValue tv = new TagValue(equipmentCode, tagName, scaledValue);
        tv.setScaledValue(scaledValue);
        tv.setQuality(TagQuality.GOOD);
        return tv;
    }

    /** 创建 BAD 质量的标签值（传感器故障） */
    public static TagValue bad(String equipmentCode, String tagName) {
        TagValue tv = new TagValue(equipmentCode, tagName, null);
        tv.setQuality(TagQuality.BAD);
        return tv;
    }

    /** 创建 UNCERTAIN 质量的标签值（超出量程） */
    public static TagValue uncertain(String equipmentCode, String tagName, Object rawValue) {
        TagValue tv = new TagValue(equipmentCode, tagName, rawValue);
        tv.setQuality(TagQuality.UNCERTAIN);
        return tv;
    }

    // ==================== 业务方法 ====================

    /** 设置换算参数并计算工程值 */
    public void setScaling(BigDecimal multiplier, BigDecimal offset) {
        this.multiplier = multiplier != null ? multiplier : BigDecimal.ONE;
        this.offset = offset != null ? offset : BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) {
            this.scaledValue = bd.multiply(this.multiplier).add(this.offset);
        }
    }

}
