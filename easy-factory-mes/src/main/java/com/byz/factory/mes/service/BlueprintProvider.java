package com.byz.factory.mes.service;

import com.byz.factory.factory.IBlueprint;

/**
 * 蓝图供应者 — 解耦 MES 对 PLM 的直接依赖。
 * <p>
 * 由 PLM 模块或测试提供实现。MES 运行时通过此接口按 blueprintCode + version
 * 获取 Blueprint（design-decisions.md §2.1）。
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * // PLM 模块提供实现
 * class PlmBlueprintProvider implements BlueprintProvider {
 *     public IBlueprint resolve(String code, String version) {
 *         return blueprintRepository.findByCodeAndVersion(code, version);
 *     }
 * }
 *
 * // 测试用内存实现
 * BlueprintProvider testProvider = (code, version) -> testBlueprintMap.get(code);
 * }</pre>
 *
 * @author 苏政
 */
@FunctionalInterface
public interface BlueprintProvider {

    /**
     * 按编码和版本解析蓝图。
     *
     * @param blueprintCode 蓝图编码
     * @param version       版本号（null 时返回最新版本）
     * @return 蓝图，未找到返回 null
     */
    IBlueprint resolve(String blueprintCode, String version);

}
