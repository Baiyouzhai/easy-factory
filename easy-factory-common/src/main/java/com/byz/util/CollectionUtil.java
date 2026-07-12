package com.byz.util;

import java.lang.reflect.Field;
import java.util.*;

public class CollectionUtil {

    public static void BeanRemove(Object obj, Object... targets) {
        if (null == targets) return;
        if (0 == targets.length) return;
        if (null == obj) return;
        if (obj.getClass().isPrimitive() || obj instanceof CharSequence) return;
        if (obj.getClass().isArray()) {
            ArrayRemove((Object[]) obj, targets);
        } else if (obj instanceof Collection<?> collection) {
            CollectionRemove(collection, targets);
        } else if (obj instanceof Map<?, ?> map) {
            MapRemove(map, targets);
        } else {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    for (Object target : targets) {
                        if (value == target) field.set(obj, null);
                        else BeanRemove(value, target);
                    }
                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void ArrayRemove(Object[] array, Object... targets) {
        if (null == targets) return;
        if (0 == targets.length) return;
        for (int i = 0; i < array.length; i++) {
            Object value = array[i];
            for (Object target : targets) {
                if (value == target) array[i] = null;
                else BeanRemove(value, target);
            }
        }
    }

    public static void CollectionRemove(Collection<?> collection, Object... targets) {
        if (null == targets) return;
        if (0 == targets.length) return;
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object value = iterator.next();
            for (Object target : targets) {
                if (value == target) iterator.remove();
                else BeanRemove(value, target);
            }
        }
    }

    public static <K> void MapRemove(Map<K, ?> map, Object... targets) {
        if (null == targets) return;
        if (0 == targets.length) return;
        for (K key : map.keySet()) {
            Object val = map.get(key);
            for (Object target : targets) {
                if (val == target) map.put(key, null);
                else BeanRemove(val, target);
            }
        }
    }

}
