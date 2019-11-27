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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.MessageReaction;
import com.stehno.ersatz.ReceivedMessage;
import com.stehno.ersatz.WsMessageType;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.stehno.ersatz.WsMessageType.*;
import static groovy.lang.Closure.DELEGATE_FIRST;
import static java.lang.System.arraycopy;

public class ReceivedMessageImpl implements ReceivedMessage {

    private Object payload;
    private WsMessageType messageType;
    private final List<MessageReactionImpl> reactions = new LinkedList<>();
    private final CountDownLatch matchLatch = new CountDownLatch(1);

    public ReceivedMessageImpl() {
        this(null, null);
    }

    public ReceivedMessageImpl(Object payload, WsMessageType messageType) {
        this.payload = payload;
        this.messageType = messageType;
    }

    @Override
    public ReceivedMessage payload(Object obj) {
        this.payload = obj;
        return this;
    }

    @Override
    public ReceivedMessage messageType(WsMessageType type) {
        this.messageType = type;
        return this;
    }

    @Override
    public MessageReaction reaction(Object payload) {
        if (resolve(payload) == BINARY) {
            return reaction(payload, BINARY);
        }
        return reaction(payload.toString(), TEXT);
    }

    @Override
    public MessageReaction reaction(Object payload, WsMessageType messageType) {
        MessageReactionImpl messageReaction = new MessageReactionImpl(payload, messageType);
        reactions.add(messageReaction);
        return messageReaction;
    }

    @Override
    public MessageReaction reaction(@DelegatesTo(value = MessageReaction.class, strategy = DELEGATE_FIRST) Closure closure) {
        return reaction(ConsumerWithDelegate.create(closure));
    }

    @Override
    public MessageReaction reaction(Consumer<MessageReaction> config) {
        MessageReactionImpl reaction = new MessageReactionImpl();
        config.accept(reaction);
        reactions.add(reaction);
        return reaction;
    }

    public boolean matches(final BufferedBinaryMessage message) {
        byte[] incoming = new byte[0];

        for (final ByteBuffer b : message.getData().getResource()) {
            final byte[] data = new byte[b.remaining()];
            b.get(data);
            incoming = merge(incoming, data);
        }

        return messageType == BINARY && incoming.equals(payload);
    }

    private static byte[] merge(final byte[] first, final byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        arraycopy(first, 0, combined, 0, first.length);
        arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

    public boolean matches(BufferedTextMessage message) {
        return messageType == TEXT && message.getData().equals(payload);
    }

    public void mark() {
        matchLatch.countDown();
    }

    public boolean marked(final long timeout, final TimeUnit unit) {
        try {
            matchLatch.await(timeout, unit);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public Object getPayload() {
        return payload;
    }

    public WsMessageType getMessageType() {
        return messageType;
    }

    public List<MessageReactionImpl> getReactions() {
        return reactions;
    }
}