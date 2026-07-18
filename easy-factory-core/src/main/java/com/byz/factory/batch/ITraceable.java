package com.byz.factory.batch;

import java.time.Instant;

/**
 * 可追溯 — 每个资源操作都必须回答：
 * <ul>
 *   <li><b>谁</b> (operator) 在什么时候操作了什么</li>
 *   <li><b>什么</b> (resource) 在哪个批次(batch)的哪个工序(process)中被操作</li>
 *   <li><b>怎么做</b> (action) 的，结果如何</li>
 * </ul>
 *
 * @author 苏政
 * @see IBatch
 */
public interface ITraceable {

    /** 所属批号 */
    String getBatchNo();

    /** 所属工序编码 */
    String getProcessCode();

    /** 所属动作编码 */
    String getActionCode();

    /** 操作人 */
    String getOperator();

    /** 操作时间戳 */
    Instant getTimestamp();

    /** 操作类型（ADD / USE / CONVERT / SPLIT...） */
    String getOperationType();

    /** 操作前的资源快照（JSON） */
    String getBeforeSnapshot();

    /** 操作后的资源快照（JSON） */
    String getAfterSnapshot();

    /** 备注 */
    String getRemark();

}
