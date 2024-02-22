package com.nrpc.exception;

public class RpcException extends RuntimeException{
    public RpcException(String msg){
        super(msg);
    }
}
