package com.byz.data;

import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

public abstract class DataExpand extends Data implements IDataExpand {

    protected JSONObject expand = new JSONObject();

    @Override
    public Map<String, Object> getExpandProperties() {
        return expand;
    }

}
