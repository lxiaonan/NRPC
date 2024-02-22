package com.nrpc.register;

import com.nrpc.common.ServiceInfo;

public interface RegisterService {
    /**
     * 服务注册
     * @param serviceInfo 注册的服务对象
     */
    void register(ServiceInfo serviceInfo) throws Exception;

    /**
     * 服务注销
     * @param serviceInfo 服务对象
     */
    void unRegister(ServiceInfo serviceInfo)throws Exception;

    /**
     * 注销服务注册器
     */
    void destroy()throws Exception;
}
