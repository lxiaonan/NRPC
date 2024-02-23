package com.nrpc.client.transport.socket;

import com.nrpc.client.transport.RequestMetadata;
import com.nrpc.client.transport.RpcClient;
import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
@Slf4j
public class SocketRpcClient implements RpcClient {
    @Override
    public MessageProtocol<RpcResponse> sendRequest(RequestMetadata metadata) throws Exception {
        // 获取服务器地址和端口，构建 socket address
        InetSocketAddress socketAddress = new InetSocketAddress(metadata.getAddress(), metadata.getPort());
        try (Socket socket = new Socket()) {
            // 与服务器建立连接
            socket.connect(socketAddress);
            // 注意：SocketClient 发送和接受的数据为：RpcRequest, RpcResponse
            // 发送数据给服务端
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(metadata.getProtocol().getData());
            oos.flush();
            // 阻塞等待服务端的响应
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            RpcResponse response = (RpcResponse) ois.readObject();
            // readObject阻塞获得响应
            MessageProtocol<RpcResponse> result = new MessageProtocol<>();
            result.setData(response);
            // 封装请求头
            MessageHeader messageHeader = new MessageHeader();
            messageHeader.setStatus(new Byte("0"));
            result.setMessageHeader(messageHeader);
            return result;
        }catch (Exception e){
            log.debug("sendRequest: {}",e.getCause().getMessage());
            throw new RuntimeException(e);
        }
    }
}
