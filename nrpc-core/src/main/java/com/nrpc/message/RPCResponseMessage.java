package com.nrpc.message;

import lombok.Data;
import lombok.ToString;
@Data
@ToString(callSuper = true)// 用于在自动生成toString方法时包含父类的字段
public class RPCResponseMessage extends Message
{
    /**
     * 调用方法的返回值
     */
    private Object returnValue;
    /**
     * 异常值
     */
    private Exception exceptionValue;


    @Override
    public int getMessageType() {
        return RPC_RESPONSE_MESSAGE;
    }
}
