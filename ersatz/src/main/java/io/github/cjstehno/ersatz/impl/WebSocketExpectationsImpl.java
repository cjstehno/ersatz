/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.InboundMessage;
import io.github.cjstehno.ersatz.cfg.MessageType;
import io.github.cjstehno.ersatz.cfg.OutboundMessage;
import io.github.cjstehno.ersatz.cfg.WaitFor;
import io.github.cjstehno.ersatz.cfg.WebSocketExpectations;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.cfg.WaitFor.FOREVER;

/**
 * Implementation of the WebSocketExpectations.
 */
@RequiredArgsConstructor @Slf4j
public class WebSocketExpectationsImpl implements WebSocketExpectations {

    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private final List<InboundMessageImpl> inboundMessages = new LinkedList<>();
    private final List<OutboundMessageImpl> outboundMessages = new LinkedList<>();
    @Getter private final String path;

    @Override public InboundMessage receives(final Object payload, final MessageType messageType) {
        val message = new InboundMessageImpl(payload, messageType);
        inboundMessages.add(message);
        return message;
    }

    @Override public InboundMessage receives(final Consumer<InboundMessage> config) {
        val message = new InboundMessageImpl();
        config.accept(message);
        inboundMessages.add(message);
        return message;
    }

    @Override public OutboundMessage sends(final Object payload, final MessageType messageType) {
        val message = new OutboundMessageImpl(payload, messageType);
        outboundMessages.add(message);
        return message;
    }

    @Override public OutboundMessage sends(final Consumer<OutboundMessage> config) {
        val message = new OutboundMessageImpl();
        config.accept(message);
        outboundMessages.add(message);
        return message;
    }

    /**
     * Marks that the websocket has connected for this expectation.
     */
    public void connect() {
        connectionLatch.countDown();
    }

    /**
     * Determines whether this expectation has connected.
     *
     * @return true if the websocket has connected for this expectation
     */
    public boolean isConnected() {
        return connectionLatch.getCount() == 0;
    }

    /**
     * Retrieves the number of messages that are expected.
     *
     * @return the expected count
     */
    public int getExpectedMessageCount() {
        return inboundMessages.size();
    }

    /**
     * Iterates over the outbound messages with the given consumer.
     *
     * @param consumer the iteration consumer
     */
    public void eachSender(final Consumer<OutboundMessageImpl> consumer) {
        outboundMessages.forEach(consumer);
    }

    /**
     * Iterates over the inbound messages with the given consumer.
     *
     * @param consumer the iteration consumer
     */
    public void eachMessage(final Consumer<InboundMessageImpl> consumer) {
        inboundMessages.forEach(consumer);
    }

    /**
     * Finds a websocket expectation matching the provided message.
     *
     * @param message the message
     * @return the message expectation
     */
    public Optional<InboundMessageImpl> findMatch(final Object message) {
        if (message instanceof BufferedTextMessage) {
            return findMatch((BufferedTextMessage) message);
        } else if (message instanceof BufferedBinaryMessage) {
            return findMatch((BufferedBinaryMessage) message);
        } else {
            return Optional.empty();
        }
    }

    private Optional<InboundMessageImpl> findMatch(final BufferedTextMessage message) {
        return inboundMessages.stream().filter(m -> m.matches(message)).findFirst();
    }

    private Optional<InboundMessageImpl> findMatch(final BufferedBinaryMessage message) {
        return inboundMessages.stream().filter(m -> m.matches(message)).findFirst();
    }

    /**
     * Verifies that the configured expectations have been met.
     *
     * @param waitFor the amount of time verification should wait before considering a timeout
     * @return true if the verification conditions have been met
     */
    public boolean verify(final WaitFor waitFor) {
        return waitForLatch(waitFor) && inboundMessages.stream().allMatch(m -> m.marked(waitFor));
    }

    private boolean waitForLatch(final WaitFor waitFor) {
        try {
            if (waitFor == FOREVER) {
                connectionLatch.await();
                return true;

            } else {
                return connectionLatch.await(waitFor.getTime(), waitFor.getUnit());
            }

        } catch (InterruptedException e) {
            return false;
        }
    }
}
