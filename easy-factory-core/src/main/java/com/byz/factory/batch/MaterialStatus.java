package com.byz.factory.batch;

/**
 * 物料状态 — 仓储中物料的质量状态。
 * <p>
 * 用于收货明细行和库存快照中的物料质量标记。
 * 典型流转：来料 → QUARANTINE（待检）→ QMS 来料检 → RELEASED（放行）或 REJECTED（拒收）。
 *
 * @author 苏政
 */
public enum MaterialStatus {

    /** 待检 — 来料入库后在隔离区等待 QMS 检验 */
    QUARANTINE,
    /** 已放行 — 检验合格，可用于生产 */
    RELEASED,
    /** 已拒收 — 检验不合格，待退货或报废 */
    REJECTED

}
