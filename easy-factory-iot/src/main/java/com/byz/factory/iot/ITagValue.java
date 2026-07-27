package com.byz.factory.iot;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 采集标签值抽象 — 供 QMS/Equip 跨模块引用设备实时数据。
 * <p>
 * IoT 从设备采集原始值并换算为工程值，通过此接口对外暴露。
 * {@link TagQuality} 标识 OPC UA 等协议的数据质量位。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>Equip</b> — 写入设备参数的实际值</li>
 *   <li><b>QMS</b> — SPC 分析时读取实时参数值</li>
 *   <li><b>MES</b> — 工序执行时读取环境参数判断生产条件</li>
 * </ul>
 *
 * @author 苏政
 */
public interface ITagValue {

    /** 数据质量 — 参照 OPC UA StatusCode 质量位 */
    enum TagQuality {
        /** 良好 — 数据可信 */
        GOOD,
        /** 坏值 — 传感器故障/通信中断 */
        BAD,
        /** 不确定 — 超出量程/初始值 */
        UNCERTAIN
    }

    /** 设备编码 */
    String getEquipmentCode();

    /** 标签名（如 NS=2;S=Temperature） */
    String getTagName();

    /** 原始值 */
    Object getValue();

    /** 换算后工程值（原始值 × multiplier + offset） */
    BigDecimal getScaledValue();

    /** 数据质量 */
    TagQuality getQuality();

    /** 设备端采集时间戳 */
    Instant getTimestamp();

    /** 服务器接收时间戳 */
    Instant getServerTimestamp();

    /** 批次 ID（用于批量存储） */
    String getBatchId();

}
