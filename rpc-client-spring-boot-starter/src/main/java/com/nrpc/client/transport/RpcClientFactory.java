package com.nrpc.client.transport;

public class RpcClientFactory {
    public static RpcClient getRpcClient() {return new NettyRpcClient();}
}
