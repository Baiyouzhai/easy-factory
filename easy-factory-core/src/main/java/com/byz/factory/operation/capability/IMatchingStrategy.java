package com.byz.factory.operation.capability;

import com.byz.factory.factory.IFactory;
import com.byz.factory.factory.IBlueprint;

/**
 * 匹配策略 — 定义"蓝图动作如何匹配到工厂设备"的算法。
 * <p>
 * 不同策略应对不同生产场景：
 * <ul>
 *   <li>{@link DirectEquipmentStrategy} — 直接匹配，忽略产线</li>
 *   <li>{@link LineFirstStrategy} — 线优先，整线匹配，线坏了换线</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IMatchingStrategy {

    /** 策略名称 */
    String getName();

    /** 执行匹配 */
    ProcessRouteMatcher.RouteMatchResult match(IBlueprint blueprint, IFactory factory);

}
