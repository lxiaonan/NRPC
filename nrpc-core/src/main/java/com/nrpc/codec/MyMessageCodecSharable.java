package com.nrpc.codec;


import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.config.Config;
import com.nrpc.message.Message;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import com.nrpc.protocol.MsgType;
import com.nrpc.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import static com.nrpc.codec.ProtocolConstants.*;


/**
 * 需要配合LengthFieldBasedFrameDecoder 编码器使用
 * Sharable可以被EventLoop共享使用
 * MessageToMessageCodec 完整的消息，转为对象a
 *   @description: 自定义消息编解码器
 *  @author xiaonan
 *  @date 2024/2/18
 *
 */
@ChannelHandler.Sharable
public class MyMessageCodecSharable<T> extends MessageToMessageCodec<ByteBuf, MessageProtocol<T>> {
    /*
    +-----------------------------------------------------------------+
    | 魔数 4byte | 协议版本号 1byte | 序列化算法 1byte | 指令类型 1byte    |
    +-----------------------------------------------------------------+
    | 请求序列 4byte | 状态 1byte | 数据长度 4byte  |  消息 ID 不定        |
    +-----------------------------------------------------------------+
    */
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol<T> msg, List<Object> list) throws Exception {
        MessageHeader header = msg.getMessageHeader();
        T data = msg.getData();
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        // 魔数
        out.writeInt(header.getMagicNum());// 4byte
        // 版本号
        out.writeByte(header.getVersion());// 1byte
        // 序列化算法类型 0：jdk 1：json
        // ordinal可以得到顺序
        byte serializeAlgorithm = header.getSerializeAlgorithm();
        out.writeByte(serializeAlgorithm);// 1byte
        // 指令类型
        out.writeByte(header.getMessageType());// 1byte
        //请求序列
        out.writeInt(header.getSequenceId());// 4byte
        // 状态
        out.writeByte(header.getStatus());// 1byte
        // 获取内容字节数组
        byte[] byteArray = Config.getSerializerAlgorithm(serializeAlgorithm).serialize(data);
        // 数据长度
        out.writeInt(byteArray.length);// 4byte
        // 写入内容
        out.writeBytes(byteArray);
        list.add(out);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 读四个字节的魔数
        int magicNum = in.readInt();
        // 读一个字节版本号
        byte version = in.readByte();
        //  读一个字节序列化算法类型
        byte serializeAlgorithm = in.readByte();
        //  读一个字节指令类型
        byte messageType = in.readByte();
        //  读四个字节请求序列
        int sequenceId = in.readInt();
        byte status = in.readByte();// 读一个字节填充数
        // 数据长度
        int len = in.readInt();
        // 读出内容
        byte[] byteArray = new byte[len];
        in.readBytes(byteArray, 0, len);
        MessageHeader header = new MessageHeader();
        header.setMagicNum(magicNum);
        header.setVersion(version);
        header.setSerializeAlgorithm(serializeAlgorithm);
        header.setStatus(status);
        header.setSequenceId(sequenceId);
        header.setMessageType(messageType);
        header.setDataLength(len);
        // 获取反序列化算法
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializeAlgorithm];
        // 反序列化
        if(messageType == MsgType.REQUEST.getType()){
            RpcRequest request = algorithm.deserialize(RpcRequest.class, byteArray);// 反序列化
            MessageProtocol<RpcRequest> protocol = new MessageProtocol<>();
            protocol.setMessageHeader(header);
            protocol.setData(request);
            out.add(protocol);
        }
        else if(messageType == MsgType.RESPONSE.getType()){
            RpcResponse response = algorithm.deserialize(RpcResponse.class, byteArray);// 反序列化
            MessageProtocol<RpcResponse> protocol = new MessageProtocol<>();
            protocol.setMessageHeader(header);
            protocol.setData(response);
            out.add(protocol);
        }
    }
}
