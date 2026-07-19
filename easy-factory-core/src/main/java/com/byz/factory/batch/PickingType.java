package com.byz.factory.batch;

/**
 * 拣料类型 — WMS 拣料策略分类。
 * <p>
 * 不同生产模式需要不同的物料配送策略。
 *
 * @author 苏政
 */
public enum PickingType {

    /** 全量拣料 — 工单开工前一次性拣齐 */
    FULL,
    /** 分阶段拣料 — 按工序/阶段分批配送 */
    STAGED,
    /** 准时化拣料 — 生产线触发按需拉动 */
    JIT

}
