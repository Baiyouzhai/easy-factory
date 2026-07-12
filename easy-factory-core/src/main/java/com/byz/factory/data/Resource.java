package com.byz.factory.data;


import com.byz.data.IData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource implements IData {

    protected String name;
    protected Dict.SourceGroup group;
    protected Dict.SourceType type;
    protected BigDecimal number;

}
