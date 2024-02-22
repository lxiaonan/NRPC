package com.nrpc.message;

public class PongMessage extends Message
{

    @Override
    public int getMessageType() {
        return PongMessage;
    }
}
