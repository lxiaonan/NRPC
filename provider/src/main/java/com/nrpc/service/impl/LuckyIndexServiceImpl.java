package com.nrpc.service.impl;

import com.nrpc.interfaces.LuckyIndexService;
import com.nrpc.server.annotation.RpcService;

import java.util.Random;
@RpcService(interfaceType = LuckyIndexService.class, version = "1.0")

public class LuckyIndexServiceImpl implements LuckyIndexService {
    @Override
    public int getLuckyIndex() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }
}
