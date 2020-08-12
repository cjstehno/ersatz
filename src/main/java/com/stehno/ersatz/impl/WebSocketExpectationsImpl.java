/**
 * Copyright (C) 2020 Christopher J. Stehno
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

import com.stehno.ersatz.cfg.ReceivedMessage;
import com.stehno.ersatz.cfg.SentMessage;
import com.stehno.ersatz.cfg.WebSocketExpectations;
import com.stehno.ersatz.cfg.WsMessageType;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.stehno.ersatz.cfg.WsMessageType.*;
import static groovy.lang.Closure.DELEGATE_FIRST;

public class WebSocketExpectationsImpl implements WebSocketExpectations {

    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private final List<ReceivedMessageImpl> receivedMessages = new LinkedList<>();
    private final List<SentMessageImpl> sentMessages = new LinkedList<>();
    private final String path;

    public WebSocketExpectationsImpl(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void connect() {
        connectionLatch.countDown();
    }

    public boolean isConnected() {
        return connectionLatch.getCount() == 0;
    }

    @Override
    public ReceivedMessage receive(Object payload) {
        if (resolve(payload) == BINARY) {
            return receive(payload, BINARY);
        }
        return receive(payload.toString(), TEXT);
    }

    @Override
    public ReceivedMessage receive(Object payload, WsMessageType messageType) {
        ReceivedMessageImpl message = new ReceivedMessageImpl(payload, messageType);
        receivedMessages.add(message);
        return message;
    }

    @Override
    public ReceivedMessage receive(@DelegatesTo(value = ReceivedMessage.class, strategy = DELEGATE_FIRST) Closure closure) {
        return receive(ConsumerWithDelegate.create(closure));
    }

    @Override
    public SentMessage send(Object payload) {
        if (resolve(payload) == BINARY) {
            return send(payload, BINARY);
        }
        return send(payload.toString(), TEXT);
    }

    @Override
    public SentMessage send(Object payload, WsMessageType messageType) {
        SentMessageImpl message = new SentMessageImpl(payload, messageType);
        sentMessages.add(message);
        return message;
    }

    @Override
    public SentMessage send(@DelegatesTo(value = SentMessage.class, strategy = DELEGATE_FIRST) Closure closure) {
        return send(ConsumerWithDelegate.create(closure));
    }

    @Override
    public ReceivedMessage receive(Consumer<ReceivedMessage> config) {
        ReceivedMessageImpl message = new ReceivedMessageImpl();
        config.accept(message);
        receivedMessages.add(message);
        return message;
    }

    @Override
    public SentMessage send(Consumer<SentMessage> config) {
        SentMessageImpl message = new SentMessageImpl();
        config.accept(message);
        sentMessages.add(message);
        return message;
    }

    public int getExpectedMessageCount() {
        return receivedMessages.size();
    }

    public void eachSender(Consumer<SentMessageImpl> consumer) {
        sentMessages.forEach(consumer);
    }

    public void eachMessage(Consumer<ReceivedMessageImpl> consumer) {
        receivedMessages.forEach(consumer);
    }

    public Optional<ReceivedMessageImpl> findMatch(final Object message){
        if( message instanceof BufferedTextMessage){
            return findMatch((BufferedTextMessage) message);
        } else if( message instanceof  BufferedBinaryMessage ){
            return findMatch((BufferedBinaryMessage) message);
        } else {
            return Optional.empty();
        }
    }

    private Optional<ReceivedMessageImpl> findMatch(final BufferedTextMessage message) {
        return receivedMessages.stream().filter(m -> m.matches(message)).findFirst();
    }

    private Optional<ReceivedMessageImpl> findMatch(final BufferedBinaryMessage message) {
        return receivedMessages.stream().filter(m -> m.matches(message)).findFirst();
    }

    public boolean verify(final long timeout, final TimeUnit unit) {
        return waitForLatch(timeout, unit) && receivedMessages.stream().allMatch(m -> m.marked(timeout, unit));
    }

    private boolean waitForLatch(final long timeout, final TimeUnit unit){
        try {
            return connectionLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }
}
