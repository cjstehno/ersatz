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

import io.github.cjstehno.ersatz.cfg.MessageReaction;
import io.github.cjstehno.ersatz.cfg.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Implementation of the MessageReaction for websockets.
 */
@NoArgsConstructor @AllArgsConstructor @Getter
public class MessageReactionImpl implements MessageReaction {

    private Object payload;
    private MessageType messageType;

    @Override public MessageReaction payload(Object obj) {
        this.payload = obj;
        return this;
    }

    @Override public MessageReaction messageType(MessageType type) {
        this.messageType = type;
        return this;
    }
}
