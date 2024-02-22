package com.nrpc.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Classname RpcClientProperties
 * @Description rpc客户端的自定义配置
 *
 */
@Data
@ConfigurationProperties("nrpc.client")
public class RpcClientProperties {

    /**
     *  负载均衡
     */
    private String balance;

    /**
     *  序列化
     */
    private String serialization;

    /**
     *  服务发现地址
     */
    private String discoveryAddr = "192.168.150.101:2181";

    /**
     *  服务调用超时
     */
    private Integer timeout;

}
