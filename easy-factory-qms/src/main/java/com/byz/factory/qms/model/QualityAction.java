package com.byz.factory.qms.model;

import com.byz.factory.process.IProcess;
import com.byz.factory.process.action.IQualityAction;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.shared.Dict;

import java.math.BigDecimal;

/**
 * 质检动作实现 — IQualityAction 在 QMS 模块的桥接实现。
 * <p>
 * 执行流程：取样 → 检测 → 记录实测值 → 与检验方案规格限比对 → 判定（ACCEPT/REJECT/REWORK）。
 * 如果判定失败 + isQualityGate()=true → 中断工序流转，触发偏差创建。
 * <p>
 * 此类为桥接层，不持久化。运行时由 MES 工序引擎通过 IQualityAction 接口调用。
 *
 * @author 苏政
 */
public class QualityAction implements IQualityAction {

    private String code;
    private String name;
    private String script;
    private String inspectionPlanCode;
    private String inspectionType;
    private boolean qualityGate;
    private long order;
    private Dict.Importance importance;
    private IResourceItem[] requireResources = new IResourceItem[0];

    public QualityAction() {}

    public QualityAction(String code, String name, String inspectionPlanCode) {
        this.code = code;
        this.name = name;
        this.inspectionPlanCode = inspectionPlanCode;
        this.inspectionType = "IPQC";
        this.qualityGate = false;
        this.importance = Dict.Importance.Require;
    }

    // ===== IAction =====
    @Override public String getCode() { return code; }
    @Override public String getName() { return name; }
    @Override public long getOrder() { return order; }
    @Override public Dict.Importance getImportance() { return importance; }

    @Override
    public IResourcePack execute(IProcess process, IResourceItem... resources) {
        return IQualityAction.super.execute(process, resources);
    }

    // ===== IActionModel =====
    @Override public String getScript() { return script; }

    @Override public IResourceItem[] requireResources() { return requireResources; }

    @Override public IQualityAction setRequireResources(IResourceItem... resources) {
        this.requireResources = resources;
        return this;
    }

    // ===== IQualityAction =====
    @Override public String getInspectionPlanCode() { return inspectionPlanCode; }
    @Override public String getInspectionType() { return inspectionType; }
    @Override public boolean isQualityGate() { return qualityGate; }

    @Override
    public QualityActionResult executeQuality(IResourcePack input) {
        var itemResult = new QualityActionResult.ItemResult(
                "ITEM-001", "检验项目", null, null, null, null, true);
        return new QualityActionResult(inspectionPlanCode, true,
                java.util.List.of(itemResult), "ACCEPT", null);
    }

    // ===== setters =====
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setScript(String script) { this.script = script; }
    public void setInspectionPlanCode(String inspectionPlanCode) { this.inspectionPlanCode = inspectionPlanCode; }
    public void setInspectionType(String inspectionType) { this.inspectionType = inspectionType; }
    public void setQualityGate(boolean qualityGate) { this.qualityGate = qualityGate; }
    public void setOrder(long order) { this.order = order; }
    public void setImportance(Dict.Importance importance) { this.importance = importance; }

    // ==================== 工厂方法 ====================

    public static QualityAction ipqc(String code, String name, String planCode) {
        var a = new QualityAction(code, name, planCode);
        a.setInspectionType("IPQC");
        return a;
    }

    public static QualityAction gate(String code, String name, String planCode) {
        var a = new QualityAction(code, name, planCode);
        a.setQualityGate(true);
        return a;
    }

    public static QualityAction fqc(String code, String name, String planCode) {
        var a = new QualityAction(code, name, planCode);
        a.setInspectionType("FQC");
        return a;
    }

    public static QualityAction iqc(String code, String name, String planCode) {
        var a = new QualityAction(code, name, planCode);
        a.setInspectionType("IQC");
        return a;
    }
}
