package com.byz.factory.script;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 脚本注册表 — 管理所有已注册脚本的元数据和编译产物。
 * <p>
 * 线程安全，支持按模块/状态/ID 检索。
 *
 * @author 苏政
 */
public class ScriptRegistry {

    /** 脚本元数据: id → metadata */
    private final Map<String, ScriptMetadata> metadataMap = new ConcurrentHashMap<>();

    /** 编译产物: id → compiled */
    private final Map<String, CompiledScript> compiledMap = new ConcurrentHashMap<>();

    /** 历史版本: id → [旧版本元数据] */
    private final Map<String, List<ScriptMetadata>> history = new ConcurrentHashMap<>();

    /**
     * 注册脚本（含元数据和编译产物）
     */
    public void register(String id, ScriptMetadata metadata, CompiledScript compiled) {
        ScriptMetadata existing = metadataMap.get(id);
        if (existing != null && !existing.getVersion().equals(metadata.getVersion())) {
            history.computeIfAbsent(id, k -> new ArrayList<>()).add(existing);
        }
        metadataMap.put(id, metadata);
        compiledMap.put(id, compiled);
    }

    /**
     * 按 ID 获取脚本元数据
     */
    public Optional<ScriptMetadata> getMetadata(String id) {
        return Optional.ofNullable(metadataMap.get(id));
    }

    /**
     * 按 ID 获取编译产物
     */
    public Optional<CompiledScript> getCompiled(String id) {
        return Optional.ofNullable(compiledMap.get(id));
    }

    /**
     * 按模块列出所有脚本
     */
    public List<ScriptMetadata> listByModule(String module) {
        return metadataMap.values().stream()
            .filter(m -> module.equals(m.getModule()))
            .collect(Collectors.toList());
    }

    /**
     * 按状态列出脚本
     */
    public List<ScriptMetadata> listByStatus(String status) {
        return metadataMap.values().stream()
            .filter(m -> status.equals(m.getStatus()))
            .collect(Collectors.toList());
    }

    /**
     * 获取活跃脚本列表
     */
    public List<ScriptMetadata> listActive() {
        return listByStatus("ACTIVE");
    }

    /**
     * 获取某个脚本的历史版本
     */
    public List<ScriptMetadata> getHistory(String id) {
        return history.getOrDefault(id, Collections.emptyList());
    }

    /**
     * 废弃某个脚本版本
     */
    public void deprecate(String id) {
        metadataMap.computeIfPresent(id, (k, v) -> {
            v.setStatus("DEPRECATED");
            return v;
        });
    }

    /**
     * 获取注册总数
     */
    public int size() {
        return metadataMap.size();
    }

}
