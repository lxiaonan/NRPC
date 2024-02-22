package com.nrpc.codec;


import com.nrpc.config.Config;
import com.nrpc.protocol.MessageHeader;
import com.nrpc.protocol.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 *  编码器
 *
 * @Author: xiaonan
 */
@Slf4j
public class RpcEncoder<T> extends MessageToByteEncoder<MessageProtocol<T>> {
    /*
    +-----------------------------------------------------------------+
    | 魔数 4byte | 协议版本号 1byte | 序列化算法 1byte | 指令类型 1byte    |
    +-----------------------------------------------------------------+
    | 请求序列 4byte | 状态 1byte | 数据长度 4byte  |  消息 ID 不定        |
    +-----------------------------------------------------------------+
    */
    /**
     *
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol<T> msg, ByteBuf byteBuf) throws Exception {
        MessageHeader header = msg.getMessageHeader();
        T data = msg.getData();
        // 魔数
        byteBuf.writeInt(header.getMagicNum());// 4byte
        // 版本号
        byteBuf.writeByte(header.getVersion());// 1byte
        // 序列化算法类型 0：jdk 1：json
        // ordinal可以得到顺序
        byte serializeAlgorithm = header.getSerializeAlgorithm();
        byteBuf.writeByte(serializeAlgorithm);// 1byte
        // 指令类型
        byteBuf.writeByte(header.getMessageType());// 1byte
        //请求序列
        byteBuf.writeInt(header.getSequenceId());// 4byte
        // 状态
        byteBuf.writeByte(header.getStatus());// 1byte
        // 获取内容字节数组
        byte[] byteArray = Config.getSerializerAlgorithm(serializeAlgorithm).serialize(data);
        // 数据长度
        byteBuf.writeInt(byteArray.length);// 4byte
        // 写入内容
        byteBuf.writeBytes(byteArray);
    }
}
