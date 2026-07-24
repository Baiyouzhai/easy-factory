package com.byz.factory.qms.service;

import com.byz.factory.batch.DeviationDisposition;
import com.byz.factory.batch.DeviationSeverity;
import com.byz.factory.qms.model.Deviation;

import java.util.List;

/**
 * 偏差管理服务接口 — QMS 模块。
 * <p>
 * 管理偏差全流程：创建 → 调查 → 处置 → 解决 → 关闭。
 * <b>跨模块协作方向：</b>
 * <ul>
 *   <li>订阅 {@code qms.inspection.failed} — 自动创建偏差</li>
 *   <li>订阅 {@code mes.process.interrupted} — 工序中断触发偏差评估</li>
 *   <li>发布 {@code qms.deviation.created} — 通知 Andon 创建呼叫</li>
 *   <li>发布 {@code qms.deviation.resolved} — 通知 MES 恢复工序</li>
 *   <li>发布 {@code qms.deviation.dispositioned} — 通知 DMS 触发审批流</li>
 * </ul>
 *
 * @author 苏政
 */
public interface DeviationService {

    /** 创建偏差 */
    Deviation createDeviation(String code, String name, String source,
                              String inspectionNo, DeviationSeverity severity);

    /** 开始调查 */
    Deviation startInvestigation(String deviationCode, String investigator);

    /** 完成调查（记录根因+影响评估） */
    Deviation completeInvestigation(String deviationCode, String rootCause,
                                    String productImpact);

    /** QA 处置判定（返工/让步/拒收） */
    Deviation dispose(String deviationCode, DeviationDisposition disposition,
                      String dispositionNote, String disposedBy);

    /** 关联 CAPA */
    Deviation linkCapa(String deviationCode, String capaCode);

    /** 标记已解决 */
    Deviation resolve(String deviationCode);

    /** 关闭偏差 */
    Deviation close(String deviationCode);

    /** 取消偏差 */
    Deviation cancel(String deviationCode, String reason);

    /** 按编号查询偏差 */
    Deviation findDeviation(String deviationCode);

    /** 按检验单号查询偏差列表 */
    List<Deviation> findDeviationsByInspection(String inspectionNo);

    /** 按工单号查询偏差列表 */
    List<Deviation> findDeviationsByWorkOrder(String workOrderNo);

    /** 获取活跃偏差（未关闭） */
    List<Deviation> getActiveDeviations();

}
