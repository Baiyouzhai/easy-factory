package com.byz.factory.plm.bom;

import com.byz.factory.factory.IBillOfMaterial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BOM 转化结果 — 规则引擎执行后的产物。
 * <p>
 * 包含转化后的 BOM、应用的规则清单和执行状态。
 *
 * @author 苏政
 */
public class BOMConversionResult {

    private final String requestId;
    private final BOMType sourceType;
    private final BOMType targetType;
    private final IBillOfMaterial resultBOM;
    private final List<String> appliedRules;
    private final List<String> warnings;
    private final boolean success;
    private final String errorMessage;

    private BOMConversionResult(Builder builder) {
        this.requestId = builder.requestId;
        this.sourceType = builder.sourceType;
        this.targetType = builder.targetType;
        this.resultBOM = builder.resultBOM;
        this.appliedRules = Collections.unmodifiableList(new ArrayList<>(builder.appliedRules));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
    }

    public String getRequestId() { return requestId; }
    public BOMType getSourceType() { return sourceType; }
    public BOMType getTargetType() { return targetType; }
    public IBillOfMaterial getResultBOM() { return resultBOM; }
    public List<String> getAppliedRules() { return appliedRules; }
    public List<String> getWarnings() { return warnings; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }

    /** 转化是否有产生差异 */
    public boolean hasChanges() {
        return success && !appliedRules.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String requestId;
        private BOMType sourceType;
        private BOMType targetType;
        private IBillOfMaterial resultBOM;
        private final List<String> appliedRules = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private boolean success = true;
        private String errorMessage;

        public Builder requestId(String v) { this.requestId = v; return this; }
        public Builder sourceType(BOMType v) { this.sourceType = v; return this; }
        public Builder targetType(BOMType v) { this.targetType = v; return this; }
        public Builder resultBOM(IBillOfMaterial v) { this.resultBOM = v; return this; }
        public Builder addAppliedRule(String rule) { this.appliedRules.add(rule); return this; }
        public Builder addWarning(String warning) { this.warnings.add(warning); return this; }
        public Builder success(boolean v) { this.success = v; return this; }
        public Builder errorMessage(String v) { this.errorMessage = v; return this; }

        public BOMConversionResult build() {
            return new BOMConversionResult(this);
        }
    }
}
