package com.byz.factory.qms.model;

import com.byz.factory.batch.InspectionType;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 检验方案 — 定义对特定产品/工序的检验项目、规格限和抽样方案。
 * <p>
 * 继承 BaseEntity（无状态机）。检验方案是参考文档，非生命周期驱动的实体。
 * 结构化字段为主（检验项目/方法/规格限），非标字段通过 IExpand 扩展。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   qms.plan.productCode      — 适用产品编码
 *   qms.plan.processCode      — 适用工序编码
 *   qms.plan.aql              — 允收质量水平
 *   qms.plan.sampleSize       — 抽样数
 *   qms.plan.standard         — 检验标准（如 GB/T 2828.1）
 *   qms.plan.version          — 版本号
 *   qms.plan.approvedBy       — 批准人
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionPlan extends BaseEntity {

    /** 适用产品编码 */
    private String productCode;

    /** 适用工序编码 */
    private String processCode;

    /** 检验类型 */
    private InspectionType inspectionType;

    /** AQL 允收质量水平 */
    private double aql;

    /** 抽样数 */
    private int sampleSize;

    /** 检验标准（如 GB/T 2828.1） */
    private String standard;

    /** 版本号 */
    private String version;

    /** 批准人 */
    private String approvedBy;

    /** 检验项目列表 */
    private List<InspectionItem> items;

    /**
     * @param code         方案编码
     * @param name         方案名称
     * @param productCode  适用产品编码
     * @param processCode  适用工序编码
     * @param inspectionType 检验类型
     */
    public InspectionPlan(String code, String name, String productCode,
                          String processCode, InspectionType inspectionType) {
        super(code, name);
        this.productCode = productCode;
        this.processCode = processCode;
        this.inspectionType = inspectionType;
        this.items = new ArrayList<>();
        this.version = "1.0";
    }

    // ==================== 业务方法 ====================

    /**
     * 添加检验项目。
     *
     * @param item 检验项目
     */
    public void addItem(InspectionItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        markUpdated();
    }

    /**
     * 更新版本号。
     *
     * @param newVersion 新版本号
     */
    public void updateVersion(String newVersion) {
        this.version = newVersion;
        markUpdated();
    }

    /**
     * 批准检验方案。
     *
     * @param approvedBy 批准人
     */
    public void approve(String approvedBy) {
        this.approvedBy = approvedBy;
        markUpdated();
    }

    /**
     * 获取检验项目总数。
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ==================== 内部类 ====================

    /**
     * 检验项目 — 检验方案中的单条检验规格定义。
     * <p>
     * 定义检验项目、规格限、检验方法和抽样方案。
     */
    @Data
    public static class InspectionItem {

        /** 项目编码 */
        private String itemCode;

        /** 项目名称（如: 粘度、pH值、外观、含量） */
        private String itemName;

        /** 规格类型（计量/计数） */
        private String specType;

        /** 规格上限 */
        private java.math.BigDecimal usl;

        /** 规格下限 */
        private java.math.BigDecimal lsl;

        /** 目标值 */
        private java.math.BigDecimal target;

        /** 单位 */
        private String unit;

        /** 检验方法 */
        private String method;

        /** 抽样方案 */
        private String sampling;

        /** 序号 */
        private int order;

        /** 是否关键质量属性(CQA) */
        private boolean critical;

        public InspectionItem() {}

        public InspectionItem(String itemCode, String itemName, String specType) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.specType = specType;
        }

    }

}
