package com.nrpc.client.handler;

import com.nrpc.client.cache.LocalRpcResponseCache;
import com.nrpc.common.RpcResponse;
import com.nrpc.protocol.MessageProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcResponseHandler extends SimpleChannelInboundHandler<MessageProtocol<RpcResponse>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol<RpcResponse> msg) throws Exception {
        // 收到响应请求
        LocalRpcResponseCache.fillResponse(msg.getMessageHeader().getSequenceId(),msg);
        log.debug("receive response:{}",msg);
    }
}
