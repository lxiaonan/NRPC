package com.nrpc.server;

import com.nrpc.common.ServiceInfo;
import com.nrpc.common.ServiceUtil;
import com.nrpc.register.RegisterService;
import com.nrpc.server.annotation.RpcService;
import com.nrpc.server.cache.LocalServiceCache;
import com.nrpc.server.config.RpcServerProperties;
import com.nrpc.server.transport.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;

import java.net.InetAddress;

@Slf4j
public class RpcServerProvide implements BeanPostProcessor, CommandLineRunner {
    private RpcServerProperties rpcServerProperties;
    private RpcServer rpcServer;
    private RegisterService registerService;

    public RpcServerProvide(RpcServerProperties rpcServerProperties, RpcServer rpcServer, RegisterService registerService) {
        this.registerService = registerService;
        this.rpcServerProperties = rpcServerProperties;
        this.rpcServer = rpcServer;
    }

    /**
     * springBoot启动时执行
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        new Thread(() -> rpcServer.start(rpcServerProperties.getPort())).start();
        log.info(" rpc server :{} start, appName :{} , port :{}", rpcServer, rpcServerProperties.getAppName(), rpcServerProperties.getPort());
        // 注册了一个JVM关闭钩子（Shutdown Hook），当JVM即将关闭时会执行zk关闭
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                registerService.destroy();
            }catch (Exception e){

            }
        }));
    }

    /**
     * 在bean实例化之后，判断是否加了@RpcService注解，如果是，则将其注册到zk中
     *
     * @param bean     实例
     * @param beanName 实例名称
     * @return
     * @throws
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
        if (annotation != null) {
            try {
                String serviceName = annotation.interfaceType().getName();
                String version = annotation.version();
                // ServiceUtil.serviceKey(serviceName, version)将接口名字和版本用-拼接
                LocalServiceCache.put(ServiceUtil.serviceKey(serviceName, version), bean);
                // 构建服务信息
                ServiceInfo serviceInfo = new ServiceInfo();
                // 该服务的ip地址
                serviceInfo.setAddress(InetAddress.getLocalHost().getHostAddress());
                serviceInfo.setPort(rpcServerProperties.getPort());
                serviceInfo.setVersion(annotation.version());
                serviceInfo.setAppName(rpcServerProperties.getAppName());
                serviceInfo.setServiceName(ServiceUtil.serviceKey(serviceName, version));
                System.err.println("服务注册成功：" + serviceInfo);
                registerService.register(serviceInfo);
            } catch (Exception e) {
                log.error("服务注册出错:{}", e);
            }
        }
        return bean;
    }
}
