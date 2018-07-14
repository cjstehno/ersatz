package com.stehno.ersatz.impl

import com.stehno.ersatz.ReceivedMessage
import com.stehno.ersatz.WsMessageType
import groovy.transform.NotYetImplemented
import io.undertow.websockets.core.BufferedBinaryMessage
import io.undertow.websockets.core.BufferedTextMessage

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import static com.stehno.ersatz.WsMessageType.TEXT

class ReceivedMessageImpl implements ReceivedMessage {

    Object payload
    WsMessageType messageType

    private CountDownLatch matchLatch = new CountDownLatch(1)

    @Override
    ReceivedMessage payload(Object obj) {
        this.payload = obj
        this
    }

    @Override
    ReceivedMessage messageType(WsMessageType type) {
        this.messageType = type
        this
    }

    @NotYetImplemented
    boolean matches(BufferedBinaryMessage message) {
        // FIXME: implement
    }

    boolean matches(BufferedTextMessage message) {
        [
            messageType == TEXT,
            message.data == payload
        ].every()
    }

    void mark() {
        matchLatch.countDown()
    }

    boolean marked(final long timeout, final TimeUnit unit) {
        matchLatch.await(timeout, unit)
    }
}
