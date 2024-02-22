package com.nrpc.service.impl;

import com.nrpc.interfaces.HelloService;
import com.nrpc.server.annotation.RpcService;

@RpcService(interfaceType = HelloService.class, version = "1.0")
public class HelloServiceImpl  implements HelloService {
    @Override
    public String sayHello(String msg) {
        return "你好，" + msg;
    }
}
