package com.nrpc.client.transport;


import com.nrpc.common.RpcRequest;
import com.nrpc.protocol.MessageProtocol;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname RequestMetadata
 * @Description 请求元数据
 * 请求远程方法，需要知道远程地址，端口，协议等
 */
@Data
@Builder
public class RequestMetadata implements Serializable {

    /**
     *  协议
     */
    private MessageProtocol<RpcRequest> protocol;

    /**
     *  地址
     */
    private String address;

    /**
     *  端口
     */
    private Integer port;

    /**
     *  服务调用超时 秒
     */
    private Integer timeout;

}
