package com.byz.factory.data;

import com.byz.data.IData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActionGroup extends Action {

    protected LinkedHashMap<Action, Dict.Importance> actions;

    public ActionGroup() {
        super();
        actions = new LinkedHashMap<>();
    }

    public ActionGroup(String name, String code) {
        super(name, code);
    }


}
