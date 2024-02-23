package com.nrpc.server.transport.netty;

import com.nrpc.codec.MyMessageCodecSharable;
import com.nrpc.codec.RpcDecoder;
import com.nrpc.codec.RpcEncoder;
import com.nrpc.protocol.ProtocolFrameDecoder;
import com.nrpc.server.handler.RpcRequestHandler;
import com.nrpc.server.transport.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * netty实现服务器通讯
 */
@Slf4j
public class NettyRpcServer implements RpcServer {
    @Override
    public void start(int port) {

        NioEventLoopGroup work = new NioEventLoopGroup();
        NioEventLoopGroup boos = new NioEventLoopGroup();
        MyMessageCodecSharable<Object> CODEC_SHARABLE = new MyMessageCodecSharable<>();
        NettyRpcRequestHandler RPC_REQUEST_HANDLER = new NettyRpcRequestHandler();
        try {
            // 获得本机ip
            String serverAddress = InetAddress.getLocalHost().getHostAddress();
            ChannelFuture channelFuture = new ServerBootstrap()
                    .group(work, boos)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ProtocolFrameDecoder());// 可变长消息解码器
                            pipeline.addLast(CODEC_SHARABLE);// 编解码器
//                            pipeline.addLast(new RpcEncoder<>());
//                            // 协议解码
//                            pipeline.addLast(new RpcDecoder());
                            pipeline.addLast(new IdleStateHandler(20, 0, 0));
                            // ChannelDuplexHandler 可以同时作为入站和出站处理器
                            pipeline.addLast(new ChannelDuplexHandler() {
                                // 用来触发特殊事件 IdleStateEvent
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    // 触发了读空闲事件
                                    if (event.state() == IdleState.READER_IDLE) {
                                        log.debug("已经 8s 没有读到数据了");
                                        // 关闭该客户端连接
                                        ctx.channel().close();
                                    }
                                }
                            });
                            pipeline.addLast(RPC_REQUEST_HANDLER);// rpc请求处理器
                        }
                    })
                    //设置子Channel的选项，这里是设置保持长连接。
                    //ChannelOption.SO_KEEPALIVE 表示是否开启TCP底层心跳机制，保证长连接不会因为长时间没有数据传输而被断开
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(serverAddress, port).sync();
            log.info("server addr {} started on port {}", serverAddress, port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.debug("server started exception", e);
        } finally {
            work.shutdownGracefully();
            boos.shutdownGracefully();
        }
    }
}
