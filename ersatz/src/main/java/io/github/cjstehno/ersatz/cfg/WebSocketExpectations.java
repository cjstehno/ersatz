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

import static io.github.cjstehno.ersatz.cfg.MessageType.*;

/**
 * Defines the expectations for web socket support.
 */
public interface WebSocketExpectations {

    /**
     * Configures an expectation that the web socket connection will receive a message with the specified payload - the payload type
     * being determined by the object type.
     *
     * @param payload the payload object
     * @return a reference to this ReceivedMessage
     */
    default InboundMessage receive(Object payload) {
        if (resolve(payload) == BINARY) {
            return receive(payload, BINARY);
        }
        return receive(payload.toString(), TEXT);
    }

    /**
     * Configures an expectation that the web socket connection will receive a message with the specified payload, with the specified
     * payload type.
     *
     * @param payload     the payload object
     * @param messageType the message type
     * @return a reference to this ReceivedMessage
     */
    InboundMessage receive(Object payload, MessageType messageType);

    /**
     * Configures an expectation that the web socket connection will receive a message configured by the closure.
     *
     * @param closure the configuration closure
     * @return a reference to this ReceivedMessage
     */
    // FIXME: groovy
//    InboundMessage receive(@DelegatesTo(value = ReceivedMessage.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Configures an expectation that the web socket connection will receive a message configured by the consumer.
     *
     * @param config the configuration consumer
     * @return a reference to this ReceivedMessage
     */
    InboundMessage receive(Consumer<InboundMessage> config);

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param payload the message payload
     * @return a reference to this ReceivedMessage
     */
    default OutboundMessage send(Object payload){
        if (resolve(payload) == BINARY) {
            return send(payload, BINARY);
        }
        return send(payload.toString(), TEXT);
    }

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param payload     the message payload
     * @param messageType the message type
     * @return a reference to this ReceivedMessage
     */
    OutboundMessage send(Object payload, MessageType messageType);

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param closure the message configuration closure
     * @return a reference to this ReceivedMessage
     */
    // FIXME: Groovy
//    OutboundMessage send(@DelegatesTo(value = SentMessage.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param config the configuration consumer
     * @return a reference to this ReceivedMessage
     */
    OutboundMessage send(Consumer<OutboundMessage> config);
}
