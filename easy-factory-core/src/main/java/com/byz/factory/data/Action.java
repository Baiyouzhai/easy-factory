package com.byz.factory.data;

import com.byz.data.IData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action implements IData {

    protected String name;
    protected String code;
    protected Dict.Execute executeType;

    public Action(String name, String code) {
        this(name, code, Dict.Execute.Noting);
    }

}
