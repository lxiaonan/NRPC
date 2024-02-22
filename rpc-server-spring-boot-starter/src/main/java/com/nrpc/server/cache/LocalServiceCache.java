package com.nrpc.server.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalServiceCache {
    private static final Map<String, Object> serverCacheMap = new ConcurrentHashMap<>();

    public static void put(String serviceName, Object value) {
        serverCacheMap.put(serviceName, value);
    }

    public static Object get(String serviceName) {
        return serverCacheMap.get(serviceName);
    }

    public static void remove(String key) {
        serverCacheMap.remove(key);
    }
}
