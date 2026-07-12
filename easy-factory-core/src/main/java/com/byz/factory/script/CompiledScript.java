package com.byz.factory.script;

/**
 * 编译后的脚本 — 引擎编译产物，可复用执行。
 * <p>
 * 封装引擎特定的编译结果，对外不暴露实现细节。
 *
 * @author 苏政
 */
public interface CompiledScript {

    /**
     * 脚本唯一标识
     *
     * @return 脚本ID
     */
    String getScriptId();

    /**
     * 编译时间戳
     *
     * @return 编译时间（epoch millis）
     */
    long getCompiledAt();

    /**
     * 是否仍然有效（未被废弃）
     *
     * @return true 如果有效
     */
    boolean isValid();

}
