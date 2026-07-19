package com.byz.factory.factory.physical;

import java.util.*;

/**
 * 设备-动作绑定实现。
 *
 * @author 苏政
 */
public class EquipmentBinding implements IEquipmentBinding {

    private final String equipmentCode;
    private final Set<String> supportedActionCodes;
    private final boolean primary;
    private final Map<String, Object> parameters;
    private final List<List<String>> supportedSequences;
    private int setupMinutes;
    private int cleanupMinutes;

    public EquipmentBinding(String equipmentCode) {
        this(equipmentCode, Collections.emptySet(), true);
    }

    public EquipmentBinding(String equipmentCode, Set<String> supportedActionCodes, boolean primary) {
        this.equipmentCode = Objects.requireNonNull(equipmentCode, "equipmentCode");
        this.supportedActionCodes = new LinkedHashSet<>(supportedActionCodes);
        this.primary = primary;
        this.parameters = new LinkedHashMap<>();
        this.supportedSequences = new ArrayList<>();
        this.setupMinutes = 0;
        this.cleanupMinutes = 0;
    }

    @Override
    public String getEquipmentCode() { return equipmentCode; }

    @Override
    public Set<String> getSupportedActionCodes() { return Collections.unmodifiableSet(supportedActionCodes); }

    @Override
    public boolean isPrimary() { return primary; }

    @Override
    public Map<String, Object> getParameters() { return parameters; }

    @Override
    public int getSetupMinutes() { return setupMinutes; }

    @Override
    public int getCleanupMinutes() { return cleanupMinutes; }

    @Override
    public List<List<String>> getSupportedSequences() {
        return Collections.unmodifiableList(supportedSequences);
    }

    // ---- Fluent API ----

    public EquipmentBinding addAction(String actionCode) {
        supportedActionCodes.add(actionCode);
        return this;
    }

    public EquipmentBinding addActions(String... actionCodes) {
        supportedActionCodes.addAll(Arrays.asList(actionCodes));
        return this;
    }

    public EquipmentBinding setPrimary(boolean primary) {
        return new EquipmentBinding(equipmentCode, supportedActionCodes, primary);
    }

    public EquipmentBinding setParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public EquipmentBinding setSetupMinutes(int minutes) {
        this.setupMinutes = minutes;
        return this;
    }

    public EquipmentBinding setCleanupMinutes(int minutes) {
        this.cleanupMinutes = minutes;
        return this;
    }

    /**
     * 声明一个动作序列。序列中的动作自动添加到 supportedActionCodes。
     * 多次调用声明多条可选序列（如正向和逆向）。
     */
    public EquipmentBinding addSequence(String... actionCodes) {
        List<String> seq = List.of(actionCodes);
        supportedSequences.add(seq);
        supportedActionCodes.addAll(seq);
        return this;
    }

    @Override
    public String toString() {
        return equipmentCode + " → " + supportedActionCodes + (primary ? " [主]" : " [备]");
    }
}
