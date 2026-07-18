package com.byz.factory.shared;

import com.byz.factory.lifecycle.ILifecycle;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生命周期实体基类 — 组合 BaseEntity + ILifecycle 状态机 + HasStatus。
 * <p>
 * 适用于具有明确状态转换规则的实体（工单、检验单、安灯呼叫、文档等）。
 * <p>
 * 用法：
 * <pre>{@code
 * public class MesWorkOrder extends BaseLifecycleEntity<WorkOrderStatus> implements IWorkOrder {
 *     public MesWorkOrder(String code, String name) {
 *         super(code, name, WorkOrderStatus.CREATED);
 *     }
 *     // 自动获得: transition(), canTransition(), getStatus()
 * }
 * }</pre>
 *
 * @param <S> 状态枚举类型，必须实现 ILifecycle.StatusEnum
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseLifecycleEntity<S extends ILifecycle.StatusEnum>
        extends BaseEntity implements ILifecycle<S>, HasStatus<S> {

    /** 当前状态 */
    private S status;

    protected BaseLifecycleEntity() {
        super();
    }

    protected BaseLifecycleEntity(String code, String name, S initialStatus) {
        super(code, name);
        this.status = initialStatus;
    }

    @Override
    public S getStatus() {
        return status;
    }

    @Override
    public void setStatus(S status) {
        this.status = status;
    }

}
