package com.byz.factory.script;

import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourceItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认脚本执行上下文实现
 *
 * @author 苏政
 */
public class DefaultScriptContext implements ScriptContext {

    private final IProcess process;
    private final IResourceItem[] inputResources;
    private final Map<String, Object> services;
    private final Map<String, Object> parameters;
    private final List<String> logEntries;
    private Object result;

    public DefaultScriptContext(IProcess process, IResourceItem... inputResources) {
        this.process = process;
        this.inputResources = inputResources != null ? inputResources : new IResourceItem[0];
        this.services = new LinkedHashMap<>();
        this.parameters = new LinkedHashMap<>();
        this.logEntries = new ArrayList<>();
    }

    @Override
    public IProcess getProcess() {
        return process;
    }

    @Override
    public IResourceItem[] getInputResources() {
        return inputResources;
    }

    @Override
    public Map<String, Object> getServices() {
        return services;
    }

    /**
     * 添加白名单服务
     */
    public DefaultScriptContext addService(String name, Object service) {
        services.put(name, service);
        return this;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * 设置参数
     */
    public DefaultScriptContext setParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    @Override
    public void log(String level, String message) {
        logEntries.add("[" + level + "] " + message);
    }

    /**
     * 获取所有日志条目
     */
    public List<String> getLogEntries() {
        return Collections.unmodifiableList(logEntries);
    }

    @Override
    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public Object getResult() {
        return result;
    }

}
