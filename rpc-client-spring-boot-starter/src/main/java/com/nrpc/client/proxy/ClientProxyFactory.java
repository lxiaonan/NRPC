package com.nrpc.client.proxy;

import com.nrpc.client.config.RpcClientProperties;
import com.nrpc.client.transport.RpcClient;
import com.nrpc.discovery.DiscoveryService;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ClientProxyFactory {


    /**
     * 获取代理对象
     *
     * @param clazz            接口
     * @param version          服务版本
     * @param discoveryService 服务注册
     * @param properties       配置 使用的负载均衡策略，序列化算法等
     * @return 代理对象
     */
    public <T> T getProxy(Class<T> clazz, String version, DiscoveryService discoveryService, RpcClientProperties properties, RpcClient rpcClient) {
        return (T) Proxy
                .newProxyInstance(
                        clazz.getClassLoader(),
                        new Class[]{clazz},
                        new ClientInvocationHandler(rpcClient,discoveryService, properties, clazz, version)
                );
    }
}
