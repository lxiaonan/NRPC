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
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<MessageProtocol<RpcRequest>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol<RpcRequest> msg) throws Exception {
        MessageProtocol<RpcResponse> resProtocol = new MessageProtocol<>();
        RpcResponse response = new RpcResponse();
        MessageHeader header = msg.getMessageHeader();
        // 设置头部消息类型为响应
        header.setMessageType(MsgType.RESPONSE.getType());
        try {
            // 处理请求
            Object object = handleRequest(msg.getData());
            header.setStatus(MsgStatus.SUCCESS.getCode());
            response.setData(object);
            resProtocol.setData(response);
            resProtocol.setMessageHeader(header);;
        } catch (Exception e) {
            // 失败
            header.setStatus(MsgStatus.FAIL.getCode());
            response.setExceptionMessage(e);
            resProtocol.setData(response);
            resProtocol.setMessageHeader(header);;
            log.error("process request {} error", header.getSequenceId(), e);
        }
        ctx.writeAndFlush(resProtocol);
    }

    /**
     * 反射调用实现类处理请求
     * @param request 请求访问的方法
     * @return 代理对象
     */
    public Object handleRequest(RpcRequest request){
        Object bean = LocalServiceCache.get(request.getServiceName());
        if (bean == null) {
            throw new RuntimeException(String.format("service not exist: %s !", request.getServiceName()));
        }
        try {
            return bean.getClass().getMethod(request.getMethod(),request.getParameterTypes())
                    .invoke(bean, request.getParameters());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
