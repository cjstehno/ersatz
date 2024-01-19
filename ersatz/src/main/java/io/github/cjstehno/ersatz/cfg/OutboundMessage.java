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
package io.github.cjstehno.ersatz.cfg;

/**
 * Defines a message being sent from the websocket handler.
 */
public interface OutboundMessage {

    /**
     * Specifies the payload for the message, based on the message type.
     *
     * @param obj the message payload
     * @return a reference to the message instance
     */
    OutboundMessage payload(Object obj);

    /**
     * Specifies the message type for the payload.
     *
     * @param type the message type
     * @return a reference to the message instance
     */
    OutboundMessage messageType(MessageType type);
}
