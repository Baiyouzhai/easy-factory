package com.byz.factory.factory.physical;

import java.util.*;

/**
 * 工位实现。
 *
 * @author 苏政
 */
public class Workstation implements IWorkstation {

    private final String code;
    private final String name;
    private final StationType stationType;
    private final List<IEquipmentBinding> equipmentBindings;

    public Workstation(String code, String name) {
        this(code, name, StationType.SINGLE);
    }

    public Workstation(String code, String name, StationType stationType) {
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.stationType = Objects.requireNonNull(stationType, "stationType");
        this.equipmentBindings = new ArrayList<>();
    }

    @Override
    public String getCode() { return code; }
    @Override
    public String getName() { return name; }
    @Override
    public StationType getStationType() { return stationType; }
    @Override
    public List<IEquipmentBinding> getEquipmentBindings() { return Collections.unmodifiableList(equipmentBindings); }

    // ---- Fluent API ----

    public Workstation addEquipment(IEquipmentBinding binding) {
        equipmentBindings.add(binding);
        return this;
    }

    public Workstation addEquipment(String equipmentCode, String... actionCodes) {
        equipmentBindings.add(new EquipmentBinding(equipmentCode).addActions(actionCodes));
        return this;
    }

    @Override
    public String toString() {
        return "[" + code + "] " + name + " " + stationType + " 设备:" + equipmentBindings.size() + "台";
    }
}
