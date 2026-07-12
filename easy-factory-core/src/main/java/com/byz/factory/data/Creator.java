package com.byz.factory.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Creator {

    private static final Creator Instance = new Creator();

    public static String GenerateSerialNumber(String name) {
        return Instance.getSerialNumber(name);
    }

    protected Map<String, AtomicLong> serialNumberMap;

    public Creator() {
        serialNumberMap = new ConcurrentHashMap<>();
    }

    public String getSerialNumber(String name) {
        long now = System.currentTimeMillis();
        AtomicLong atomicLong = serialNumberMap.get(name);
        if (null == atomicLong) {
            atomicLong = new AtomicLong(now);
            serialNumberMap.put(name, atomicLong);
        } else {
            if (atomicLong.get() < now) {
                atomicLong.set(now);
            }
        }
        return String.format("%s%06d", name, atomicLong.incrementAndGet());
    }

}
