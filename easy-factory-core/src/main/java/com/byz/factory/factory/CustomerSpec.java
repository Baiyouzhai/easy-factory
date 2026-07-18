package com.byz.factory.factory;

import com.byz.factory.resource.IResourcePack;

/**
 * 客户规格书 — PLM阶段定义：引用蓝图骨架，标注物料需求和质量标准。
 * <p>
 * 这是蓝图的第一个应用阶段：产品设计师选择蓝图，标注"需要什么材料、达到什么标准"。
 * 不涉及设备——那是 EngineeringBOM 的事。
 *
 * @author 苏政
 */
public class CustomerSpec {

    private String specCode;
    private String productCode;
    private String customerName;
    private IBlueprint blueprint;
    private IResourcePack materialRequirements;   // 物料需求
    private String qualityStandard;               // 质量标准编码(如药典版本)
    private String regulatoryRequirements;        // 法规要求
    private String status;                        // DRAFT / APPROVED / OBSOLETE

    public CustomerSpec() {}

    public CustomerSpec(String specCode, String productCode, IBlueprint blueprint) {
        this.specCode = specCode;
        this.productCode = productCode;
        this.blueprint = blueprint;
        this.status = "DRAFT";
    }

    public String getSpecCode() { return specCode; }
    public void setSpecCode(String specCode) { this.specCode = specCode; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public IBlueprint getBlueprint() { return blueprint; }
    public void setBlueprint(IBlueprint blueprint) { this.blueprint = blueprint; }

    public IResourcePack getMaterialRequirements() { return materialRequirements; }
    public void setMaterialRequirements(IResourcePack materialRequirements) { this.materialRequirements = materialRequirements; }

    public String getQualityStandard() { return qualityStandard; }
    public void setQualityStandard(String qualityStandard) { this.qualityStandard = qualityStandard; }

    public String getRegulatoryRequirements() { return regulatoryRequirements; }
    public void setRegulatoryRequirements(String regulatoryRequirements) { this.regulatoryRequirements = regulatoryRequirements; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
