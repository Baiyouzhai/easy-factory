package com.byz.factory.script;

/**
 * 脚本引擎抽象 — 统一脚本编译与执行的接口。
 * <p>
 * 底层可切换不同实现：
 * <ul>
 *   <li>{@code GraalScriptEngine} — GraalJS (推荐，支持沙箱)</li>
 *   <li>{@code NashornScriptEngine} — Nashorn (过渡，JDK 15+)</li>
 *   <li>{@code J2V8ScriptEngine} — V8 via J2V8</li>
 * </ul>
 * 所有脚本执行前必须通过沙箱检查，确保只能访问白名单 API。
 *
 * @author 苏政
 * @see CompiledScript
 * @see ScriptContext
 */
public interface IScriptEngine {

    /**
     * 编译脚本源码，返回可复用的编译产物。
     * 编译一次，多次执行。
     *
     * @param scriptId 脚本唯一标识
     * @param source   脚本源码
     * @return 编译后的脚本
     */
    CompiledScript compile(String scriptId, String source);

    /**
     * 执行已编译的脚本
     *
     * @param compiled 编译产物
     * @param context  执行上下文（包含授权的 API 和参数）
     * @return 执行结果
     */
    Object execute(CompiledScript compiled, ScriptContext context);

    /**
     * 执行原始字符串脚本（仅供开发/调试使用）。
     * 生产环境应使用 compile + execute 两步。
     *
     * @param source  脚本源码
     * @param context 执行上下文
     * @return 执行结果
     */
    Object eval(String source, ScriptContext context);

    /**
     * 引擎名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 是否支持沙箱
     *
     * @return true 如果支持沙箱安全限制
     */
    boolean isSandboxed();

}
