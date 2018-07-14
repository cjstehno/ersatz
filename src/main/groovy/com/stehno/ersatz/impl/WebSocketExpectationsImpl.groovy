package com.stehno.ersatz.impl

import com.stehno.ersatz.ReceivedMessage
import com.stehno.ersatz.WebSocketExpectations
import com.stehno.ersatz.WsMessageType
import io.undertow.websockets.core.BufferedBinaryMessage
import io.undertow.websockets.core.BufferedTextMessage

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import static java.util.concurrent.TimeUnit.SECONDS

class WebSocketExpectationsImpl implements WebSocketExpectations {

    private CountDownLatch connectionLatch = new CountDownLatch(1)
    private final List<ReceivedMessageImpl> receivedMessages = []

    void connect() {
        connectionLatch.countDown()

        // TODO: runs any reactions
    }

    @Override
    ReceivedMessage receive(Object payload, WsMessageType messageType) {
        ReceivedMessageImpl message = new ReceivedMessageImpl(payload: payload, messageType: messageType)
        receivedMessages << message

        message
    }

    @Override
    ReceivedMessage receive(@DelegatesTo(ReceivedMessage) Closure closure) {
        ReceivedMessageImpl message = new ReceivedMessageImpl()
        closure.delegate = message
        closure.call()
        receivedMessages << message

        message
    }

    ReceivedMessageImpl findMatch(final BufferedTextMessage message) {
        receivedMessages.find { m -> m.matches(message) }
    }

    ReceivedMessageImpl findMatch(final BufferedBinaryMessage message) {
        receivedMessages.find { m -> m.matches(message) }
    }

    // FIXME: add a timeout to the verify method (optional)
    // TODO: document this blocking
    boolean verify(final long timeout = 1, final TimeUnit unit = SECONDS) {
        connectionLatch.await(timeout, unit) && receivedMessages.every { m -> m.marked(timeout, unit) }
    }
}