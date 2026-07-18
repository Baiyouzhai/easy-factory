package com.byz.factory.script;

import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourceItem;

import java.util.Map;

/**
 * 脚本执行上下文 — 封装脚本所需的运行时信息和授权 API。
 * <p>
 * 脚本只能访问此上下文中显式暴露的对象，无法访问文件系统、网络、Java 反射等。
 *
 * @author 苏政
 */
public interface ScriptContext {

    /**
     * 当前工序
     */
    IProcess getProcess();

    /**
     * 输入资源列表
     */
    IResourceItem[] getInputResources();

    /**
     * 白名单服务 — key 为服务名（如 "mes", "qms", "lims"），
     * value 为对应的受限 API 表面对象（非完整 Service）。
     */
    Map<String, Object> getServices();

    /**
     * 脚本参数 — 额外传递的键值对
     */
    Map<String, Object> getParameters();

    /**
     * 脚本日志 — 脚本内的日志输出通过此方法收集
     */
    void log(String level, String message);

    /**
     * 设置返回值
     */
    void setResult(Object result);

    /**
     * 获取返回值
     */
    Object getResult();

}
