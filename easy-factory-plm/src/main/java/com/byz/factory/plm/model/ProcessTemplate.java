package com.byz.factory.plm.model;

import com.byz.factory.process.IProcess;
import com.byz.factory.shared.BaseEntity;
import com.byz.factory.shared.HasVersion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * PLM 工艺模板 — 继承 BaseEntity 获得 code/name/audit。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessTemplate extends BaseEntity implements HasVersion {

    private String category;
    private String version;
    private List<IProcess> processes;

    public ProcessTemplate(String code, String name, String category) {
        super(code, name);
        this.category = category;
        this.version = "0.1.0";
        this.processes = new ArrayList<>();
    }

}
