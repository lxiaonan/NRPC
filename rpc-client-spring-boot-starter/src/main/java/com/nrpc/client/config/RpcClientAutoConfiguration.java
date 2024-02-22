package com.nrpc.client.config;

import com.nrpc.balancer.LoadBalance;
import com.nrpc.balancer.RandomLoadBalance;
import com.nrpc.balancer.RoundRobinLoadBalancer;
import com.nrpc.client.processor.RpcClientProcessor;
import com.nrpc.client.proxy.ClientProxyFactory;
import com.nrpc.discovery.DiscoveryService;
import com.nrpc.discovery.ZooKeeperDiscoveryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

@Configuration
public class RpcClientAutoConfiguration {
    @Bean
    public RpcClientProperties rpcClientProperties(Environment environment) {
        // 获取配置文件中的配置信息
        BindResult<RpcClientProperties> bind = Binder.get(environment).bind("nrpc.client", RpcClientProperties.class);
        return bind.get();
    }

    /**
     * 代理工厂
     * @return 代理类
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientProxyFactory clientProxyFactory() {
        return new ClientProxyFactory();
    }

    /**
     *
     * @return 随机
     */
    @Primary // 如果存在多个bean优先选择这个
    @Bean(name = "loadBalance")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "nrpc.client", name = "balance", havingValue = "randomBalance", matchIfMissing = true)
    public LoadBalance randomBalance() {
        return new RandomLoadBalance();
    }

    /**
     *
     * @return 轮询
     */
    @Bean(name = "loadBalance")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "nrpc.client", name = "balance", havingValue = "fullRoundBalance")
    public LoadBalance loadBalance() {
        return new RoundRobinLoadBalancer();
    }

    /**
     * 服务发现
     * @param properties
     * @param loadBalance
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    // 表示当容器中存在指定类型的Bean时，才会创建并注册这个Bean。
    @ConditionalOnBean({RpcClientProperties.class, LoadBalance.class})
    public DiscoveryService discoveryService(@Autowired RpcClientProperties properties,
                                             @Autowired LoadBalance loadBalance) {
        return new ZooKeeperDiscoveryServiceImpl(properties.getDiscoveryAddr(), loadBalance);
    }

    /**
     * 客户端处理器
     * @param clientProxyFactory 代理工厂
     * @param discoveryService 服务发现
     * @param properties 配置
     * @return 客户端处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcClientProcessor rpcClientProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                                 @Autowired DiscoveryService discoveryService,
                                                 @Autowired RpcClientProperties properties){
        return new RpcClientProcessor(clientProxyFactory,discoveryService,properties);
    }
}
