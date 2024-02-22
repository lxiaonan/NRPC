package com.nrpc.util;

import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PromisesFactory {
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
}
