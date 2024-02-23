package com.nrpc.client.proxy;

import com.nrpc.client.config.RpcClientProperties;
import com.nrpc.client.transport.RequestMetadata;
import com.nrpc.client.transport.RpcClient;
import com.nrpc.client.transport.RpcClientFactory;
import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.common.ServiceInfo;
import com.nrpc.common.ServiceUtil;
import com.nrpc.discovery.DiscoveryService;
import com.nrpc.exception.RpcException;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import com.nrpc.protocol.MsgStatus;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
@Slf4j
public class ClientInvocationHandler implements InvocationHandler {
    private DiscoveryService discoveryService;
    private RpcClientProperties properties;
    private Class<?> aClass;

    private String version;
    private RpcClient rpcClient;

    public ClientInvocationHandler(RpcClient rpcClient, DiscoveryService discoveryService, RpcClientProperties properties, Class<?> aClass, String version) {
        this.rpcClient = rpcClient;
        this.discoveryService = discoveryService;
        this.properties = properties;
        this.aClass = aClass;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // zk服务发现
        ServiceInfo serviceInfo = discoveryService.discovery(ServiceUtil.serviceKey(this.aClass.getName(), version));
        if (serviceInfo == null) {
            throw new RuntimeException("server not found");
        }
        // 消息请求协议
        MessageProtocol<RpcRequest> messageProtocol = new MessageProtocol<>();
        String serialization = properties.getSerialization();
        if(serialization == null || serialization.isEmpty()){
            messageProtocol.setMessageHeader(MessageHeader.build("Java"));
        }else {
            messageProtocol.setMessageHeader(MessageHeader.build(serialization));
        }
        // 创建Rpc请求
        // 设置请求的ServiceName、Method、ParameterTypes、Parameters
        RpcRequest request = new RpcRequest();
        request.setServiceName(ServiceUtil.serviceKey(this.aClass.getName(), version));
        request.setMethod(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        messageProtocol.setData(request);
        // 构建请求元数据
        RequestMetadata metadata = RequestMetadata.builder()
                .protocol(messageProtocol).address(serviceInfo.getAddress())
                .port(serviceInfo.getPort()).timeout(properties.getTimeout()).build();
        // 发送网络请求
        MessageProtocol<RpcResponse> responseMessageProtocol = rpcClient.sendRequest(metadata);
        if (responseMessageProtocol == null) {
            log.error("请求超时");
            throw new RpcException("rpc调用结果失败， 请求超时 timeout:" + properties.getTimeout());
        }
        // 查看返回的请求头的状态是否为假
        if (!MsgStatus.isSuccess(responseMessageProtocol.getMessageHeader().getStatus())) {
            log.error("rpc调用结果失败， message：{}", responseMessageProtocol.getData().getExceptionMessage().getCause().getMessage());
            throw new RpcException(responseMessageProtocol.getData().getExceptionMessage().getCause().getMessage());
        }
        // 返回请求结果
        return responseMessageProtocol.getData().getData();
    }
}
