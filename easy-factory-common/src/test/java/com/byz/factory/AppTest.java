package com.byz.factory;

import com.alibaba.fastjson2.JSONObject;
import com.byz.data.Data;
import com.byz.data.DataExpand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * common 模块基础测试
 */
public class AppTest {

    @Test
    void testApp() {
        assertTrue(true);
    }

    /**
     * IDataExpand 手动验证（非自动化测试）
     */
    public static void main(String[] args) {
        B b = new B();
        b.name = "test";
        Object name = b.getProperty("name");
        System.out.println(name);
        b.setProperty("name", "ok");
        System.out.println(b.name);
        b.setExpandProperty("name", "test");
        System.out.println(b.name);
        System.out.println(b.getExpandProperty("name"));
        b.setProperty("age", "14");
        System.out.println(b.toJsonString(false));
        System.out.println(b.toJsonString(true));
        B a = b.jsonClone();
        System.out.println(a.toJsonString(false));
        System.out.println(a.toJsonString(true));
        B c = b.jsonClone(true);
        System.out.println(c.toJsonString(false));
        System.out.println(c.toJsonString(true));
    }

}

class C extends Data {
    public String name;
    public Map<String, Object> data;
}

class B extends DataExpand {

    public String name;
    public JSONObject temp;

    public B() {
        temp = new JSONObject();
        temp.put("expand", expand);
    }

    @Override
    public Object setNativeProperty(String key, Object value) {
        if ("name".equals(key)) {
            String old = name;
            name = null == value ? null : value.toString();
            return old;
        }
        return null;
    }

    @Override
    public JSONObject getExpandProperties() {
        return expand;
    }

}
