package com.nrpc.server.transport.socket;

import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.server.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
@Slf4j
public class SocketRpcRequestHandler implements Runnable {
    private Socket socket;

    public SocketRpcRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        log.debug("The server handle client message by thread {}.", Thread.currentThread().getName());
        // 返回结果
        RpcResponse response = new RpcResponse();
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            // 读取客户端请求的 RPC 请求对象
            RpcRequest request = (RpcRequest) inputStream.readObject();
            try {
                // 通过反射获取结果
                Object object = RpcRequestHandler.handleRequest(request);
                response.setData(object);
            } catch (Exception e) {
                // 写入异常结果
                response.setExceptionMessage(new RuntimeException("处理客户端请求异常："+e.getCause().getMessage()));
            }
            log.debug("远程调用结果{}",response);
            outputStream.writeObject(response);
        } catch (Exception e) {
            log.debug("The server handle client message error", e);
        }
    }
}
