package com.nrpc.client.transport;

import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.protocol.MessageProtocol;

public interface RpcClient {
    /**
     *  发送rpc请求
     * @param metadata 请求的元数据信息
     * @return 调用结果
     */
    MessageProtocol<RpcResponse> sendRequest(RequestMetadata metadata) throws Exception;
}
