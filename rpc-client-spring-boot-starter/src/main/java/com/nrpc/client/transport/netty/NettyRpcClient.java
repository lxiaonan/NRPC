package com.nrpc.client.transport.netty;

import com.nrpc.client.cache.LocalRpcResponseCache;
import com.nrpc.client.handler.RpcResponseHandler;
import com.nrpc.client.transport.RequestMetadata;
import com.nrpc.client.transport.RpcClient;
import com.nrpc.client.transport.RpcFuture;
import com.nrpc.codec.MyMessageCodecSharable;
import com.nrpc.codec.RpcDecoder;
import com.nrpc.codec.RpcEncoder;
import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.message.PingMessage;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import com.nrpc.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private final Bootstrap bootstrap;
    private final NioEventLoopGroup group;

    public NettyRpcClient() {
        group = new NioEventLoopGroup(4);
        MyMessageCodecSharable<Object> CODEC_SHARABLE = new MyMessageCodecSharable<>();
        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ProtocolFrameDecoder());
                        pipeline.addLast(CODEC_SHARABLE);
                        // 3秒钟检查一次写事件 状态是：IdleState.WRITER_IDLE
                        pipeline.addLast(new IdleStateHandler(0, 15, 0));
                        pipeline.addLast(new ChannelDuplexHandler() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.WRITER_IDLE) {
                                    // 心跳检查
//                                        log.debug("15秒没有写操作，发送心跳");
                                    ctx.writeAndFlush(new PingMessage());
                                }
                            }
                        });
//                        pipeline.addLast(new RpcEncoder<>());
//                        // 协议解码
//                        pipeline.addLast(new RpcDecoder());
                        pipeline.addLast(new RpcResponseHandler());
                    }
                });
    }

    /**
     * 发送请求
     * @param metadata 请求的元数据信息
     * @return 响应对象
     * @throws Exception
     */
    @Override
    public MessageProtocol<RpcResponse> sendRequest(RequestMetadata metadata)throws Exception {
            // 获取请求协议
            MessageProtocol<RpcRequest> protocol = metadata.getProtocol();
            // 创建一个空书包，用于存储返回结果
            RpcFuture<MessageProtocol<RpcResponse>> future = new RpcFuture<>();
            // 将空书包和对应请求序列挂载到本地缓存
            LocalRpcResponseCache.add(protocol.getMessageHeader().getSequenceId(), future);
            // 客户端建立连接
            ChannelFuture channelFuture = bootstrap.connect(metadata.getAddress(), metadata.getPort()).sync();
            // 连接操作完成后，添加了一个监听器来处理连接结果
            channelFuture.addListener((ChannelFutureListener) arg0 -> {
                if (channelFuture.isSuccess()) {
                    log.info("connect rpc server {} on port {} success.", metadata.getAddress(), metadata.getPort());
                } else {
                    log.error("connect rpc server {} on port {} failed.", metadata.getAddress(), metadata.getPort());
                    // 打印失败信息
                    channelFuture.cause().printStackTrace();
                    group.shutdownGracefully();
                }
            });
            // 写入请求协议
            channelFuture.channel().writeAndFlush(protocol);
            // 阻塞等待结果
            return metadata.getTimeout() != 0 ?
                    future.get(metadata.getTimeout(), TimeUnit.MILLISECONDS) : future.get();

    }
}
