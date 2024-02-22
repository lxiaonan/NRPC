package com.nrpc.server.transport;

/**
 * rpc服务器接口
 */
public interface RpcServer {
    /**
     * 启动服务
     * @param port 端口
     */
    void start(int port);
}
