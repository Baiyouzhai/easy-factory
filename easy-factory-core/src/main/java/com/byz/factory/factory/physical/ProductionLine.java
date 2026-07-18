package com.byz.factory.factory.physical;

import java.util.*;

/**
 * 产线实现。
 *
 * @author 苏政
 */
public class ProductionLine implements IProductionLine {

    private final String code;
    private final String name;
    private final Set<String> supportedProcessCodes;
    private final List<LineNode> nodes;

    public ProductionLine(String code, String name) {
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.supportedProcessCodes = new LinkedHashSet<>();
        this.nodes = new ArrayList<>();
    }

    @Override
    public String getCode() { return code; }
    @Override
    public String getName() { return name; }
    @Override
    public Set<String> getSupportedProcessCodes() { return Collections.unmodifiableSet(supportedProcessCodes); }
    @Override
    public List<LineNode> getNodes() { return Collections.unmodifiableList(nodes); }

    // ---- Fluent ----

    public ProductionLine addProcess(String processCode) { supportedProcessCodes.add(processCode); return this; }
    public ProductionLine addProcesses(String... codes) { supportedProcessCodes.addAll(Arrays.asList(codes)); return this; }

    /** 添加工位（自动递增序号） */
    public ProductionLine addNode(String workstationCode) {
        nodes.add(new LineNode(workstationCode, nodes.size() + 1));
        return this;
    }

    public ProductionLine addNode(LineNode node) {
        nodes.add(node);
        return this;
    }

    @Override
    public String toString() {
        return "产线[" + code + "] " + name + " 节点:" + nodes.size() + " 工序:" + supportedProcessCodes;
    }
}
