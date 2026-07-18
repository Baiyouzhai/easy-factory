package com.byz.factory.process;

import java.util.List;

/**
 * 生产路线
 *
 * @author 苏政
 */
public class ProcessRoute {

    protected String code;
    protected String name;
    protected List<IProcess> processes;

    public ProcessRoute() {
    }

    public ProcessRoute(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<IProcess> processes) {
        this.processes = processes;
    }

}
