package com.stehno.ersatz.impl

import com.stehno.ersatz.MessageReaction
import com.stehno.ersatz.MessageReactionImpl
import com.stehno.ersatz.ReceivedMessage
import com.stehno.ersatz.WsMessageType
import groovy.util.logging.Slf4j
import io.undertow.websockets.core.BufferedBinaryMessage
import io.undertow.websockets.core.BufferedTextMessage

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import static com.stehno.ersatz.WsMessageType.BINARY
import static com.stehno.ersatz.WsMessageType.TEXT
import static com.stehno.ersatz.impl.MessageTypeResolver.resolve

@Slf4j
class ReceivedMessageImpl implements ReceivedMessage {

    Object payload
    WsMessageType messageType
    final List<MessageReactionImpl> reactions = []

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

    @Override
    MessageReaction reaction(Object payload) {
        switch (resolve(payload)) {
            case BINARY:
                return reaction(payload, BINARY)
            default:
                return reaction(payload.toString(), TEXT)
        }
    }

    @Override
    MessageReaction reaction(Object payload, WsMessageType messageType) {
        MessageReactionImpl messageReaction = new MessageReactionImpl(payload: payload, messageType: messageType)
        reactions << messageReaction
        messageReaction
    }

    @Override
    MessageReaction reaction(@DelegatesTo(MessageReaction) Closure closure) {
        MessageReactionImpl reaction = new MessageReactionImpl()
        closure.delegate = reaction
        closure.call()
        reactions << reaction
        this
    }

    boolean matches(BufferedBinaryMessage message) {
        byte[] incoming = message.data.resource.collect { b ->
            byte[] data = new byte[b.remaining()]
            b.get(data)
            data
        }.flatten()

        messageType == BINARY && incoming == payload
    }

    boolean matches(BufferedTextMessage message) {
        messageType == TEXT && message.data == payload
    }

    void mark() {
        matchLatch.countDown()
    }

    boolean marked(final long timeout, final TimeUnit unit) {
        matchLatch.await(timeout, unit)
    }
}
