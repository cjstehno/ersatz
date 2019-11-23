package com.stehno.ersatz.impl;

import com.stehno.ersatz.SentMessage;
import com.stehno.ersatz.WsMessageType;

@SuppressWarnings("ConfusingMethodName")
public class SentMessageImpl implements SentMessage {

    private Object payload;
    private WsMessageType messageType;

    public SentMessageImpl(final Object payload, final WsMessageType messageType){
        this.payload = payload;
        this.messageType = messageType;
    }

    @Override public SentMessage payload(Object obj) {
        payload = obj;
        return this;
    }

    @Override public SentMessage messageType(WsMessageType type) {
        messageType = type;
        return this;
    }

    public Object getPayload() {
        return payload;
    }

    public WsMessageType getMessageType() {
        return messageType;
    }
}
