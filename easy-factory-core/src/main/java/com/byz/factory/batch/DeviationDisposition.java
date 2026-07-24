package com.byz.factory.batch;

/**
 * 偏差处置方式 — QMS 模块。
 * <p>
 * QA 对质量偏差的最终判定处置方式。
 *
 * @author 苏政
 */
public enum DeviationDisposition {

    /** 返工 — 退回生产重新加工至合格 */
    REWORK,

    /** 让步接收 — 经评估后让步放行（需审批） */
    CONCESSION,

    /** 拒收/报废 — 不合格，不可使用 */
    REJECT

}
