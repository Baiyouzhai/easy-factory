package com.byz.factory.equip;

import com.byz.factory.lifecycle.ILifecycle;
import com.byz.factory.resource.IMachine;

import java.time.LocalDate;

/**
 * 设备台账抽象 — 供跨模块引用 Equipment 实体。
 * <p>
 * 继承 {@link IMachine}（资源视角：group=Machine）+
 * {@link ILifecycle}&lt;{@link MachineStatus}&gt;（状态机视角），
 * 补充设备台账特有字段。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>EAM</b> — {@code IAsset.getEquipmentCode()} 通过此接口查询设备台账</li>
 *   <li><b>MES</b> — 工单开工前通过此接口检查设备状态</li>
 *   <li><b>APS</b> — 排程时通过此接口读取设备产能日历</li>
 *   <li><b>Andon</b> — 故障报警时通过此接口获取设备位置/型号</li>
 *   <li><b>BottleneckDetector</b> — 瓶颈识别时通过此接口获取设备信息</li>
 *   <li><b>EngineeringBOM</b> — 标注时通过此接口引用设备编码</li>
 * </ul>
 *
 * <h3>与 IMachine 的关系</h3>
 * {@link IMachine} 是"设备作为资源"（能干啥、group=Machine），
 * {@code IEquipment} 是"设备作为台账"（型号、位置、资产编码、供应商）。
 * Equipment（equip 模块）同时实现两者。
 *
 * @author 苏政
 * @see IMachine
 * @see MachineStatus
 */
public interface IEquipment extends IMachine, ILifecycle<MachineStatus> {

    /** 设备型号 */
    String getModel();

    /** 设备类别（反应釜/离心机/包装机/检测仪） */
    String getCategory();

    /** 安装位置/工位 */
    String getLocation();

    /** 固定资产编码 */
    String getAssetCode();

    /** 规格参数 */
    String getSpecifications();

    /** 供应商 */
    String getSupplier();

    /** 购置日期 */
    LocalDate getPurchaseDate();

}
