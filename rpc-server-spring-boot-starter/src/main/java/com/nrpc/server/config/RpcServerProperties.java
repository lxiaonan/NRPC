package com.nrpc.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Classname RpcClientProperties
 * @Description
 */
@Data
@ConfigurationProperties(prefix = "nrpc.server")
public class RpcServerProperties {

    /**
     *  服务启动端口
     */
    private Integer port = 8090;

    /**
     *  服务名称
     */
    private String appName;

    /**
     *  注册中心地址
     */
    private String registryAddr = "127.0.0.1:2181";

}
