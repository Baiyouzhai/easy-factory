package com.byz.factory;

import com.alibaba.fastjson2.JSONObject;
import com.byz.data.Data;
import com.byz.data.DataExpand;
import com.byz.data.IData;
import com.byz.data.IDataExpand;
import com.byz.data.IExpand;
import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

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

//        C a = new C();
//        a.name = "test";
//        a.data = new JSONObject();
//        a.data.put("name", "张三");
//        System.out.println(a.toJsonString());
//        C b = a.jsonClone();
//        System.out.println(b.toJsonString());
//        System.out.println(a == b);
//        System.out.println(a.equals(b));
//        System.out.println(a.name == b.name);
//        System.out.println(a.name.equals(b.name));
//        System.out.println(a.data == b.data);
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
//        return temp.getJSONObject("expand");
    }

}
