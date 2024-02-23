package com.nrpc.server.config;

import com.nrpc.register.RegisterService;
import com.nrpc.register.ZooKeeperRegisterServiceImpl;
import com.nrpc.server.RpcServerProvide;
import com.nrpc.server.transport.netty.NettyRpcServer;
import com.nrpc.server.transport.RpcServer;
import com.nrpc.server.transport.socket.SocketRpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    @Primary // 如果存在多个bean优先选择这个
    @Bean(name = "rpcServer")
    @ConditionalOnMissingBean()// 只有在容器中不存在 RpcServer 类型的 bean 时才会执行该方法
    @ConditionalOnProperty(prefix = "nrpc.server", name = "transport", havingValue = "netty", matchIfMissing = true)
    public RpcServer nettyRpcServer() {
        // 选择netty作为网络通讯
        return new NettyRpcServer();
    }
    @Bean(name = "rpcServer")
    @ConditionalOnMissingBean()// 只有在容器中不存在 RpcServer 类型的 bean 时才会执行该方法
    @ConditionalOnProperty(prefix = "nrpc.server", name = "transport", havingValue = "socket")
    public RpcServer socketRpcServer() {
        // 选择socket作为网络通讯
        return new SocketRpcServer();
    }

    @Bean
    @ConditionalOnMissingBean(RpcServerProvide.class)
    public RpcServerProvide rpcServerProvider(@Autowired RegisterService registryService,
                                              @Autowired RpcServer rpcServer,
                                              @Autowired RpcServerProperties rpcServerProperties) {
        return new RpcServerProvide(rpcServerProperties, rpcServer, registryService);
    }

}
