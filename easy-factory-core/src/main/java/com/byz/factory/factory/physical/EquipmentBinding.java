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

    public EquipmentBinding(String equipmentCode) {
        this(equipmentCode, Collections.emptySet(), true);
    }

    public EquipmentBinding(String equipmentCode, Set<String> supportedActionCodes, boolean primary) {
        this.equipmentCode = Objects.requireNonNull(equipmentCode, "equipmentCode");
        this.supportedActionCodes = new LinkedHashSet<>(supportedActionCodes);
        this.primary = primary;
        this.parameters = new LinkedHashMap<>();
    }

    @Override
    public String getEquipmentCode() { return equipmentCode; }

    @Override
    public Set<String> getSupportedActionCodes() { return Collections.unmodifiableSet(supportedActionCodes); }

    @Override
    public boolean isPrimary() { return primary; }

    @Override
    public Map<String, Object> getParameters() { return parameters; }

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

    @Override
    public String toString() {
        return equipmentCode + " → " + supportedActionCodes + (primary ? " [主]" : " [备]");
    }
}
