package com.nrpc.server.handler;

import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import com.nrpc.protocol.MsgStatus;
import com.nrpc.protocol.MsgType;
import com.nrpc.server.cache.LocalServiceCache;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * rpc入站请求处理器
 */
public class RpcRequestHandler {
    /**
     * 反射调用实现类处理请求
     * @param request 请求访问的方法
     * @return 代理对象
     */
    public static Object handleRequest(RpcRequest request){
        // 获取实现类
        Object bean = LocalServiceCache.get(request.getServiceName());
        if (bean == null) {
            throw new RuntimeException(String.format("service not exist: %s !", request.getServiceName()));
        }
        try {
            // 反射调用方法
            return bean.getClass().getMethod(request.getMethod(),request.getParameterTypes())
                    .invoke(bean, request.getParameters());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
