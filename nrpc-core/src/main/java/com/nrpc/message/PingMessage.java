package com.nrpc.message;

public class PingMessage  extends Message
{

    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
