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
package io.github.cjstehno.ersatz.cfg;

import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.cfg.MessageType.BINARY;
import static io.github.cjstehno.ersatz.cfg.MessageType.TEXT;
import static io.github.cjstehno.ersatz.cfg.MessageType.resolve;

/**
 * Defines a message coming into the websockets handler.
 */
public interface InboundMessage {

    /**
     * The message payload, which will be resolved as either TEXT or BINARY type based on the object type.
     *
     * @param obj the payload object
     * @return a reference to this ReceivedMessage
     */
    InboundMessage payload(Object obj);

    /**
     * The message payload type.
     *
     * @param type the payload type
     * @return a reference to this ReceivedMessage
     */
    InboundMessage messageType(MessageType type);

    /**
     * Used to specify a reaction message to be sent after receiving this message. The message type will be determined
     * by the payload type.
     *
     * @param payload the payload object
     * @return a reference to this ReceivedMessage
     */
    default MessageReaction reaction(Object payload) {
        if (resolve(payload) == BINARY) {
            return reaction(payload, BINARY);
        }
        return reaction(payload.toString(), TEXT);
    }

    /**
     * Used to specify a reaction message to be sent after receiving this message.
     *
     * @param payload     the payload object
     * @param messageType the message type
     * @return a reference to this ReceivedMessage
     */
    MessageReaction reaction(Object payload, MessageType messageType);

    /**
     * Used to specify a reaction message to be sent after receiving this message.
     *
     * @param config the reaction configuration consumer
     * @return a reference to this ReceivedMessage
     */
    MessageReaction reaction(Consumer<MessageReaction> config);
}
