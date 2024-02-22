package com.nrpc.server.config;

import com.nrpc.register.RegisterService;
import com.nrpc.register.ZooKeeperRegisterServiceImpl;
import com.nrpc.server.RpcServerProvide;
import com.nrpc.server.transport.NettyRpcServer;
import com.nrpc.server.transport.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rpc服务端自动装配类
 */
@Configuration// 配置类
@EnableConfigurationProperties(RpcServerProperties.class)// 指定自动装配的配置文件
public class RpcServerAutoConfiguration {
    @Autowired
    RpcServerProperties rpcServerProperties;

    /**
     * 注册中心
     *
     * @return 选择ZooKeeper
     */
    @Bean
    @ConditionalOnMissingBean
    public RegisterService RegisterService() {
        return new ZooKeeperRegisterServiceImpl(rpcServerProperties.getRegistryAddr());
    }

    /**
     * 选择服务器
     *
     * @return 选择netty
     */
    @Bean
    @ConditionalOnMissingBean(RpcServer.class)// 只有在容器中不存在 RpcServer 类型的 bean 时才会执行该方法
    public RpcServer RpcServer() {
        // 选择netty作为网络通讯
        return new NettyRpcServer();
    }

    @Bean
    @ConditionalOnMissingBean(RpcServerProvide.class)
    public RpcServerProvide rpcServerProvider(@Autowired RegisterService registryService,
                                              @Autowired RpcServer rpcServer,
                                              @Autowired RpcServerProperties rpcServerProperties) {
        return new RpcServerProvide(rpcServerProperties, rpcServer, registryService);
    }

}
