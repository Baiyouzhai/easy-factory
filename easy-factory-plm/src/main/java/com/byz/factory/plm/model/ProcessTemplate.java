package com.byz.factory.plm.model;

import com.byz.data.DataExpand;
import com.byz.factory.model.IProcess;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * PLM 工艺模板 — 可复用的工序模板。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessTemplate extends DataExpand {

    private String code;
    private String name;
    private String category;
    private String version;
    private String status;
    private List<IProcess> processes;

    public ProcessTemplate(String code, String name, String category) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.version = "0.1.0";
        this.status = "DRAFT";
        this.processes = new ArrayList<>();
    }

}
