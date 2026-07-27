package com.byz.factory.equip.model;

import com.byz.factory.equip.IEquipmentRecipe;
import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 设备配方 — 适用于特定设备+产品的工艺参数集合，按阶段组织。
 * 实现 {@link IEquipmentRecipe} 供 EngineeringBOM/IoT/PLM 跨模块引用。
 * <p>
 * 配方由 PLM 工艺设计产生，下发到 Equip 管理，MES 执行时调用。
 * 版本通过 {@link #version} 字符串管理（如 "1.0.0"），{@link #bumpVersion()} 递增。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   equip.recipe.{code}.createdBy    — 创建人
 *   equip.recipe.{code}.approvedBy   — 审批人
 *   equip.recipe.{code}.status       — 配方状态(DRAFT/APPROVED/OBSOLETED)
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "equip_recipe")
public class EquipmentRecipe extends BaseEntity implements IEquipmentRecipe {

    /** 适用设备编码 */
    @Column(nullable = false, length = 100)
    private String equipmentCode;

    /** 适用产品编码 */
    @Column(length = 100)
    private String productCode;

    /** 配方版本（如 "1.0.0"） */
    @Column(nullable = false, length = 20)
    private String version;

    /** 配方参数阶段列表 */
    @ElementCollection
    @CollectionTable(name = "equip_recipe_phase", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<RecipePhase> phases = new ArrayList<>();

    public EquipmentRecipe() {
        super();
    }

    /**
     * @param code          配方编码
     * @param name          配方名称
     * @param equipmentCode 适用设备编码
     * @param productCode   适用产品编码
     */
    public EquipmentRecipe(String code, String name, String equipmentCode, String productCode) {
        super(code, name);
        this.equipmentCode = equipmentCode;
        this.productCode = productCode;
        this.version = "1.0.0";
    }

    /** 添加一个配方阶段 */
    public void addPhase(RecipePhase phase) {
        this.phases.add(phase);
        markUpdated();
    }

    /** 递增版本号（MINOR bump：1.0.0 → 1.1.0） */
    public void bumpVersion() {
        String[] parts = version.split("\\.");
        int minor = Integer.parseInt(parts[1]) + 1;
        this.version = parts[0] + "." + minor + ".0";
        markUpdated();
    }

    /** 获取只读的阶段列表 */
    @Override
    public List<RecipePhase> getPhases() {
        return Collections.unmodifiableList(phases);
    }

    // ==================== 内部类 ====================

    /**
     * 配方阶段 — 单个参数在特定阶段的设定值。
     * <p>
     * 示例：反应釜配方中，温度参数在"升温段"设定 120°C、持续 600 秒、升温速率 2°C/min。
     */
    @Data
    @Embeddable
    public static class RecipePhase implements IEquipmentRecipe.IRecipePhase {

        /** 参数编码 */
        @Column(length = 50)
        private String paramCode;

        /** 阶段名称（如：升温段、恒温段、降温段） */
        @Column(length = 50)
        private String phase;

        /** 设定值 */
        @Column(precision = 20, scale = 6)
        private BigDecimal setValue;

        /** 持续时间（秒） */
        private int duration;

        /** 变化速率（单位/秒） */
        @Column(precision = 10, scale = 4)
        private BigDecimal rampRate;

        public RecipePhase() {
        }

        /**
         * @param paramCode 参数编码
         * @param phase     阶段名称
         * @param setValue  设定值
         * @param duration  持续时间（秒）
         */
        public RecipePhase(String paramCode, String phase, BigDecimal setValue, int duration) {
            this.paramCode = paramCode;
            this.phase = phase;
            this.setValue = setValue;
            this.duration = duration;
            this.rampRate = BigDecimal.ZERO;
        }

        /**
         * @param paramCode 参数编码
         * @param phase     阶段名称
         * @param setValue  设定值
         * @param duration  持续时间（秒）
         * @param rampRate  变化速率（单位/秒）
         */
        public RecipePhase(String paramCode, String phase, BigDecimal setValue, int duration, BigDecimal rampRate) {
            this.paramCode = paramCode;
            this.phase = phase;
            this.setValue = setValue;
            this.duration = duration;
            this.rampRate = rampRate;
        }

    }

}
