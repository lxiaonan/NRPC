package com.nrpc.server.transport;

import com.nrpc.codec.MyMessageCodecSharable;
import com.nrpc.codec.RpcDecoder;
import com.nrpc.codec.RpcEncoder;
import com.nrpc.protocol.ProtocolFrameDecoder;
import com.nrpc.server.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
        RpcRequestHandler RPC_REQUEST_HANDLER = new RpcRequestHandler();
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
//                            pipeline.addLast(CODEC_SHARABLE);// 编解码器
                            pipeline.addLast(new RpcEncoder<>());
                            // 协议解码
                            pipeline.addLast(new RpcDecoder());
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
