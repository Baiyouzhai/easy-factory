package com.byz.factory.factory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 物料清单 (BOM)
 * <p>
 * Beta: 后续模块实现时进一步填充
 *
 * @author 苏政
 */
public class BillOfMaterial implements IBillOfMaterial {

    protected String productCode;
    protected List<MaterialLineItem> materials;

    public BillOfMaterial() {
        this.materials = new ArrayList<>();
    }

    public BillOfMaterial(String productCode) {
        this.productCode = productCode;
        this.materials = new ArrayList<>();
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public List<MaterialLineItem> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialLineItem> materials) {
        this.materials = materials;
    }

    /**
     * BOM 行项
     */
    public record MaterialLineItem(String materialCode, String materialName,
                                    BigDecimal quantity, String unit, int lineNumber) {
    }

}
