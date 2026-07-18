package com.byz.factory.event;

import java.time.Instant;

/**
 * 审计追踪记录 — GMP 数据完整性基础设施。
 * <p>
 * 记录"谁、何时、对什么实体、做了什么操作、变更前后内容、原因"。
 * 遵循 ALCOA+ 原则（可归属、清晰可读、同步记录、原始、准确）。
 * <p>
 * 典型用法：
 * <pre>{@code
 *   AuditTrail trail = new AuditTrail(
 *       "Batch", "B20260718-001", "TRANSITION",
 *       "operator01", Instant.now(),
 *       "{\"status\":\"CREATED\"}", "{\"status\":\"IN_PROGRESS\"}",
 *       "开工确认完成"
 *   );
 * }</pre>
 *
 * @author 苏政
 */
public record AuditTrail(
    /** 实体类型（如 Batch, WorkOrder, Equipment） */
    String entityType,
    /** 实体标识（如 batchNo, workOrderNo） */
    String entityId,
    /** 操作类型（CREATE, UPDATE, TRANSITION, SIGN, DELETE） */
    String action,
    /** 操作人标识 */
    String operator,
    /** 操作时间 */
    Instant timestamp,
    /** 变更前内容（JSON 快照，可为 null 表示新建） */
    String before,
    /** 变更后内容（JSON 快照，可为 null 表示删除） */
    String after,
    /** 变更原因 */
    String reason
) {}
