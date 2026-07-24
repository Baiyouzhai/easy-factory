package com.byz.factory.qms.service;

import com.byz.factory.qms.model.Capa;

import java.util.List;

/**
 * CAPA 管理服务接口 — QMS 模块。
 * <p>
 * 管理纠正与预防措施全流程：创建 → 根因分析 → 执行 → 效果验证 → 关闭。
 * <b>跨模块协作方向：</b>
 * <ul>
 *   <li>订阅 {@code qms.deviation.dispositioned} — 需要 CAPA 时自动创建</li>
 *   <li>发布 {@code qms.capa.created} — 通知 DMS 创建审批流文档</li>
 *   <li>发布 {@code qms.capa.verified} — 通知 DMS 归档</li>
 *   <li>发布 {@code qms.capa.closed} — 通知 BI 更新质量看板</li>
 * </ul>
 *
 * @author 苏政
 */
public interface CapaService {

    /** 创建 CAPA */
    Capa createCapa(String code, String name, String deviationCode,
                    String problemDescription, String assignTo);

    /** 完成根因分析 */
    Capa analyzeRootCause(String capaCode, String rootCause);

    /** 开始执行纠正/预防措施 */
    Capa executeActions(String capaCode, String correctiveAction,
                        String preventiveAction);

    /** 完成效果验证 */
    Capa verify(String capaCode, String verification, String approvedBy);

    /** 关闭 CAPA */
    Capa close(String capaCode);

    /** 取消 CAPA */
    Capa cancel(String capaCode, String reason);

    /** 按编号查询 CAPA */
    Capa findCapa(String capaCode);

    /** 按偏差编号查询 CAPA 列表 */
    List<Capa> findCapasByDeviation(String deviationCode);

    /** 获取逾期 CAPA */
    List<Capa> getOverdueCapas();

    /** 获取活跃 CAPA（未关闭） */
    List<Capa> getActiveCapas();

}
