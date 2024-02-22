package com.nrpc.codec;

import com.nrpc.common.RpcRequest;
import com.nrpc.common.RpcResponse;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import com.nrpc.protocol.MsgType;
import com.nrpc.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 解码器
 *
 * @Author: changjiu.wang
 * @Date: 2021/7/24 22:28
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * +---------------------------------------------------------------+
     * | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte|
     * +---------------------------------------------------------------+
     * | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
     * +---------------------------------------------------------------+
     * |                   数据内容 （长度不定）                         |
     * +---------------------------------------------------------------+
     * <p>
     * decode 这个方法会被循环调用
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
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
        if (messageType == MsgType.REQUEST.getType()) {
            RpcRequest request = algorithm.deserialize(RpcRequest.class, byteArray);// 反序列化
            MessageProtocol<RpcRequest> protocol = new MessageProtocol<>();
            protocol.setMessageHeader(header);
            protocol.setData(request);
            out.add(protocol);
        } else if (messageType == MsgType.RESPONSE.getType()) {
            RpcResponse response = algorithm.deserialize(RpcResponse.class, byteArray);// 反序列化
            MessageProtocol<RpcResponse> protocol = new MessageProtocol<>();
            protocol.setMessageHeader(header);
            protocol.setData(response);
            out.add(protocol);
        }

    }
}
