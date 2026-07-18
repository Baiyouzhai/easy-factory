package com.byz.factory.factory;

import java.util.*;

/**
 * 工程标注书 — 工厂工程部在蓝图上标注设备映射和工艺参数。
 * <p>
 * 这是蓝图的第二个应用阶段：客户规格书确定后，工程部选择工厂，
 * 为每个动作标注"用哪台设备做、什么参数、需要什么工装"。
 *
 * @author 苏政
 */
public class EngineeringBOM {

    private String bomCode;
    private String specCode;                  // 关联 CustomerSpec
    private String factoryCode;               // 目标工厂
    private IBlueprint blueprint;
    private Map<String, EquipmentAssignment> assignments;  // actionCode → 设备分配
    private String status;                    // DRAFT / RELEASED / OBSOLETE

    public EngineeringBOM() {
        this.assignments = new LinkedHashMap<>();
    }

    public EngineeringBOM(String bomCode, String specCode, String factoryCode, IBlueprint blueprint) {
        this();
        this.bomCode = bomCode;
        this.specCode = specCode;
        this.factoryCode = factoryCode;
        this.blueprint = blueprint;
        this.status = "DRAFT";
    }

    /** 为指定动作分配设备 */
    public EngineeringBOM assign(String actionCode, String equipmentCode, Map<String, Object> parameters) {
        assignments.put(actionCode, new EquipmentAssignment(equipmentCode, parameters));
        return this;
    }

    public EngineeringBOM assign(String actionCode, String equipmentCode) {
        return assign(actionCode, equipmentCode, Collections.emptyMap());
    }

    /** 获取某动作的设备分配 */
    public Optional<EquipmentAssignment> getAssignment(String actionCode) {
        return Optional.ofNullable(assignments.get(actionCode));
    }

    // ── getters/setters ──

    public String getBomCode() { return bomCode; }
    public void setBomCode(String bomCode) { this.bomCode = bomCode; }
    public String getSpecCode() { return specCode; }
    public void setSpecCode(String specCode) { this.specCode = specCode; }
    public String getFactoryCode() { return factoryCode; }
    public void setFactoryCode(String factoryCode) { this.factoryCode = factoryCode; }
    public IBlueprint getBlueprint() { return blueprint; }
    public void setBlueprint(IBlueprint blueprint) { this.blueprint = blueprint; }
    public Map<String, EquipmentAssignment> getAssignments() { return assignments; }
    public void setAssignments(Map<String, EquipmentAssignment> assignments) { this.assignments = assignments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** 设备分配记录 */
    public record EquipmentAssignment(String equipmentCode, Map<String, Object> parameters) {}
}
