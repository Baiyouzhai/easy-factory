package com.byz.factory.plm.model;

import com.byz.factory.factory.BlueprintStatus;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.process.IProcess;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝图 — PLM 核心实体，产品的工艺路线定义。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED），
 * 实现 IBlueprint 供 MES/LIMS/MPS 等下游模块编译期引用。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Blueprint extends BaseLifecycleEntity<BlueprintStatus> implements IBlueprint {

    /** 产品编码 */
    private String productCode;

    /** 版本号（语义版本） */
    private String version;

    /** 蓝图描述 */
    private String description;

    /** 工序清单 */
    private List<IProcess> processes;

    /** 设计人 */
    private String author;

    /** 审批人 */
    private String approvedBy;

    /**
     * @param code        蓝图编码
     * @param name        蓝图名称
     * @param productCode 产品编码
     */
    public Blueprint(String code, String name, String productCode) {
        super(code, name, BlueprintStatus.DRAFT);
        this.productCode = productCode;
        this.version = "0.1.0";
        this.processes = new ArrayList<>();
    }

    /** 获取生产工序清单（实现 IBlueprint 接口） */
    @Override
    public List<IProcess> getProductionProcessList() {
        return processes;
    }

}
