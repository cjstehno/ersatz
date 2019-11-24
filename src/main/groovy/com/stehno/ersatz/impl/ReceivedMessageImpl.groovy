/*
 * Copyright (C) 2019 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.impl

import com.stehno.ersatz.MessageReaction
import com.stehno.ersatz.ReceivedMessage
import com.stehno.ersatz.WsMessageType
import groovy.util.logging.Slf4j
import io.undertow.websockets.core.BufferedBinaryMessage
import io.undertow.websockets.core.BufferedTextMessage
import space.jasan.support.groovy.closure.ConsumerWithDelegate

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

import static com.stehno.ersatz.WsMessageType.BINARY
import static com.stehno.ersatz.WsMessageType.TEXT
import static com.stehno.ersatz.WsMessageType.resolve
import static groovy.lang.Closure.DELEGATE_FIRST

@Slf4j
class ReceivedMessageImpl implements ReceivedMessage {

    Object payload
    WsMessageType messageType
    final List<MessageReactionImpl> reactions = []

    private final CountDownLatch matchLatch = new CountDownLatch(1)

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
    MessageReaction reaction(@DelegatesTo(value = MessageReaction, strategy = DELEGATE_FIRST) Closure closure) {
        reaction(ConsumerWithDelegate.create(closure))
    }

    @Override
    MessageReaction reaction(Consumer<MessageReaction> config) {
        MessageReactionImpl reaction = new MessageReactionImpl()
        config.accept(reaction)
        reactions << reaction
        reaction
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
