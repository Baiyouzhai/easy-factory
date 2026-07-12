package com.byz.util;

import java.util.List;
import java.util.Map;

public class MapUtil {

    public static void RecursionRemove(Map<?, ?> map, Object target) {
        map.entrySet().stream().filter(kv -> kv.getValue() == target)
    }

}
