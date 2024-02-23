package com.nrpc.server.transport.socket;

import com.nrpc.server.transport.RpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
@Slf4j
public class SocketRpcServer implements RpcServer {
    // 因为是处理io请求，所以需要获取cpu线程数
    /**
     * 当前处理器数量
     */
    private final int cpuNum = Runtime.getRuntime().availableProcessors();
    // 理论线程数计算
    // 线程大小：这一点要看我们执行的任务是cpu密集型，还是io密集型
    // 如果有关于计算机计算，比较消耗资源的是cpu密集型，理论线程大小应该设置为：cpu 核数 + 1
    // 如果有关网络传输，连接数据库等，是io密集型，理论线程大小应该设置为：cpu * 2
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(cpuNum * 2,cpuNum * 2,180L, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100));
    @Override
    public void start(int port) {
        // 实现Socket通信方式的RPC服务器启动逻辑
        try (ServerSocket serverSocket = new ServerSocket()){
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            serverSocket.bind(new InetSocketAddress(hostAddress, port));
            System.out.println("RPCSocket服务器启动，监听地址：" + hostAddress + "，端口：" + port);
            // 接收请求
            Socket socket;
            // 如果没有请求会阻塞在
            // serverSocket.accept()
            while ((socket = serverSocket.accept()) != null){
                log.debug("The client connected [{}].", socket.getInetAddress());
                poolExecutor.execute(new SocketRpcRequestHandler(socket));
            }

        }catch (Exception e){
            log.debug("服务器异常",e);
        }finally {
            // 关闭线程池
            poolExecutor.shutdown();
        }
    }
}
