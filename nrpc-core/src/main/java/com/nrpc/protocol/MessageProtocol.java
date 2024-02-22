package com.nrpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 将请求消息和响应消息进行封包和解包，同时定义相关的消息类型
 * @Author:  xiaonan
 */
@Data
public class MessageProtocol<T> implements Serializable {
    /**
     * 请求头
     */
    private MessageHeader messageHeader;
    /**
     * 请求体
     */
    private T data;
}
