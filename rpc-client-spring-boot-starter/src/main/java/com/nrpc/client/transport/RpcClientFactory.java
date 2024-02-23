package com.nrpc.client.transport;

import com.nrpc.client.transport.netty.NettyRpcClient;

public class RpcClientFactory {
    public static RpcClient getRpcClient() {return new NettyRpcClient();}
}
