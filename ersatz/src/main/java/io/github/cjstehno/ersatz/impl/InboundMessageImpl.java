/**
 * Copyright (C) 2024 Christopher J. Stehno
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
import io.github.cjstehno.ersatz.cfg.MessageReaction;
import io.github.cjstehno.ersatz.cfg.MessageType;
import io.github.cjstehno.ersatz.cfg.WaitFor;
import io.github.cjstehno.ersatz.util.ByteArrays;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.cfg.MessageType.BINARY;
import static io.github.cjstehno.ersatz.cfg.MessageType.TEXT;
import static io.github.cjstehno.ersatz.cfg.WaitFor.FOREVER;

/**
 * Implementation of the InboundMessage for websocket connections.
 */
@AllArgsConstructor @NoArgsConstructor
public class InboundMessageImpl implements InboundMessage {

    @Getter private Object payload;
    @Getter private MessageType messageType;
    @Getter private final List<MessageReactionImpl> reactions = new LinkedList<>();
    private final CountDownLatch matchLatch = new CountDownLatch(1);

    @Override public InboundMessage payload(final Object obj) {
        this.payload = obj;
        return this;
    }

    @Override public InboundMessage messageType(final MessageType type) {
        this.messageType = type;
        return this;
    }

    @Override public MessageReaction reaction(final Object payload, final MessageType messageType) {
        val messageReaction = new MessageReactionImpl(payload, messageType);
        reactions.add(messageReaction);
        return messageReaction;
    }

    @Override public MessageReaction reaction(final Consumer<MessageReaction> config) {
        val reaction = new MessageReactionImpl();
        config.accept(reaction);
        reactions.add(reaction);
        return reaction;
    }

    /**
     * Determines whether the message matches the configured expectations.
     *
     * @param message the message
     * @return true if the expectations are matched
     */
    public boolean matches(final BufferedBinaryMessage message) {
        return messageType == BINARY && Arrays.equals(ByteArrays.join(message.getData().getResource()), (byte[]) payload);
    }

    /**
     * Determines whether the message matches the configured expectations.
     *
     * @param message the message
     * @return true if the expectations are matched
     */
    public boolean matches(final BufferedTextMessage message) {
        return messageType == TEXT && message.getData().equals(payload);
    }

    /**
     * Marks the message.
     */
    public void mark() {
        matchLatch.countDown();
    }

    /**
     * Determines whether the message has been marked.
     *
     * @param waitFor amount of time to wait before timing out
     * @return true if the message has been marked
     */
    public boolean marked(final WaitFor waitFor) {
        try {
            if (waitFor == FOREVER) {
                matchLatch.await();
                return true;
            } else {
                return matchLatch.await(waitFor.getTime(), waitFor.getUnit());
            }
        } catch (InterruptedException e) {
            return false;
        }
    }
}
