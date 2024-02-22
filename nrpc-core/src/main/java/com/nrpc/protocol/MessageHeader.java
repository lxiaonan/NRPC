package com.nrpc.protocol;

import com.nrpc.codec.ProtocolConstants;
import com.nrpc.serialization.Serializer;
import com.nrpc.util.UUIDToIntConverter;
import lombok.Data;

import java.util.UUID;

/**
 * 消息请求头
 *
 * @author xiaonan
 */
@Data
public class MessageHeader {
    /*
    +-----------------------------------------------------------------+
    | 魔数 4byte | 协议版本号 1byte | 序列化算法 1byte | 指令类型 1byte    |
    +-----------------------------------------------------------------+
    | 请求序列 4byte | 状态 1byte | 数据长度 4byte  |  消息内容 32byte    |
    +-----------------------------------------------------------------+
    */
    private int magicNum; // 魔数
    private byte version; // 版本
    private byte serializeAlgorithm; // 序列化算法
    private byte messageType; // 消息类型 0请求 1响应
    private int sequenceId; // 请求ID
    private byte status;// 状态 0 成功 1失败
    private int dataLength; // 数据长度

    public static MessageHeader build(String serialization) {
        MessageHeader header = new MessageHeader();
        header.magicNum = ProtocolConstants.MAGIC_NUM;
        header.version = ProtocolConstants.VERSION;
        header.serializeAlgorithm = (byte)Serializer.Algorithm.valueOf(serialization).ordinal();
        header.sequenceId = UUIDToIntConverter.uuidToInt(UUID.randomUUID());
        header.messageType = MsgType.REQUEST.getType();
        return header;
    }
}
