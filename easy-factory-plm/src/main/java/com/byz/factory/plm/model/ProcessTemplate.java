package com.byz.factory.plm.model;

import com.byz.factory.process.IProcess;
import com.byz.factory.process.IProcessParameter;
import com.byz.factory.shared.BaseEntity;
import com.byz.factory.shared.HasVersion;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * PLM 工艺模板 — 继承 BaseEntity 获得 id/code/name/audit，实现 HasVersion 支持版本管理。
 * <p>
 * 模板是蓝图的"设计母版"——工艺工程师基于模板创建具体产品的蓝图。
 * 模板不需要 ILifecycle 状态机（状态仅 DRAFT/ACTIVE/OBSOLETED 简单标记）。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   plm.template.category    — 模板类别
 *   plm.template.version     — 版本号
 *   plm.template.status      — 模板状态
 *   plm.template.author      — 创建人
 *   plm.template.processCount — 工序数量
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "plm_process_template")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProcessTemplate extends BaseEntity implements HasVersion {

    /** 类别（机加工/装配/化工/电子） */
    @Column(length = 50)
    private String category;

    /** 版本号 */
    @Column(length = 30, nullable = false)
    private String version;

    /** 状态：DRAFT / ACTIVE / OBSOLETED */
    @Column(length = 20, nullable = false)
    private String status;

    /** 工序模板列表（暂不持久化，通过关联表存储） */
    @Transient
    private List<IProcess> processes;

    /** 工艺参数集合（暂不持久化，通过关联表存储） */
    @Transient
    private List<IProcessParameter> parameters;

    public ProcessTemplate(String code, String name, String category) {
        super(code, name);
        this.category = category;
        this.version = "0.1.0";
        this.status = "DRAFT";
        this.processes = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }

    // ==================== 业务便捷方法 ====================

    /** 激活模板 */
    public void activate() {
        this.status = "ACTIVE";
        markUpdated();
    }

    /** 废弃模板 */
    public void obsolete() {
        this.status = "OBSOLETED";
        markUpdated();
    }

}
