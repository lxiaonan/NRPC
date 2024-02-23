package com.nrpc.server.transport.netty;

import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import com.nrpc.protocol.MsgStatus;
import com.nrpc.protocol.MsgType;
import com.nrpc.server.handler.RpcRequestHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NettyRpcRequestHandler extends SimpleChannelInboundHandler<MessageProtocol<RpcRequest>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol<RpcRequest> msg) throws Exception {
        MessageProtocol<RpcResponse> resProtocol = new MessageProtocol<>();
        RpcResponse response = new RpcResponse();
        MessageHeader header = msg.getMessageHeader();
        // 设置头部消息类型为响应
        header.setMessageType(MsgType.RESPONSE.getType());
        try {
            // 处理请求
            Object object = RpcRequestHandler.handleRequest(msg.getData());
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
}
