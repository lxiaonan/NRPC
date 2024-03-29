package com.nrpc.protocol;

import lombok.Getter;
/**
 * @Classname MsgStatus
 * @Description 请求状态
 */
public enum MsgStatus {
    SUCCESS((byte)0),
    FAIL((byte)1);

    @Getter
    private final byte code;

    MsgStatus(byte code) {
        this.code = code;
    }

    public static boolean isSuccess(byte code){
        return MsgStatus.SUCCESS.code == code;
    }

}
