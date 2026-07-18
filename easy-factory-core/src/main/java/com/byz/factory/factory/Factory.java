package com.byz.factory.factory;

import com.byz.factory.factory.physical.IProductionLine;
import com.byz.factory.factory.physical.IWorkstation;
import com.byz.factory.process.IProcess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工厂实现 — 工艺层 + 物理层。
 *
 * @author 苏政
 */
public class Factory implements IFactory {

    protected String code;
    protected String name;

    // 工艺层
    protected List<IProcess> processes;

    // 物理层
    protected List<IProductionLine> productionLines;
    protected Map<String, IWorkstation> workstationRegistry;

    public Factory() {
        this.productionLines = new ArrayList<>();
        this.workstationRegistry = new LinkedHashMap<>();
    }

    public Factory(String code, String name) {
        this();
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // ── 工艺层 ──

    @Override
    public List<IProcess> getProcesses() { return processes; }
    public void setProcesses(List<IProcess> processes) { this.processes = processes; }

    // ── 物理层 ──

    @Override
    public List<IProductionLine> getProductionLines() {
        if (productionLines == null) productionLines = new ArrayList<>();
        return productionLines;
    }

    public void setProductionLines(List<IProductionLine> lines) { this.productionLines = lines; }

    public Factory addLine(IProductionLine line) {
        getProductionLines().add(line);
        return this;
    }

    // ── 工位注册表 ──

    @Override
    public Map<String, IWorkstation> getWorkstationRegistry() {
        if (workstationRegistry == null) workstationRegistry = new LinkedHashMap<>();
        return workstationRegistry;
    }

    public Factory registerWorkstation(IWorkstation ws) {
        getWorkstationRegistry().put(ws.getCode(), ws);
        return this;
    }

}
