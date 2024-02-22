package com.nrpc.discovery;

import com.nrpc.common.ServiceInfo;

/**
 * 服务发现接口
 */
public interface DiscoveryService {
    /**
     *  发现
     * @param serviceName 服务名称
     * @return 服务
     * @throws Exception
     */
    ServiceInfo discovery(String serviceName) throws Exception;
}
