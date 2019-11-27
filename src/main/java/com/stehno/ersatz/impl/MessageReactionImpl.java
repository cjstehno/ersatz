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
import com.stehno.ersatz.WsMessageType;

public class MessageReactionImpl implements MessageReaction {

    private Object payload;
    private WsMessageType messageType;

    public MessageReactionImpl() {
        this(null, null);
    }

    public MessageReactionImpl(Object payload, WsMessageType messageType) {
        this.payload = payload;
        this.messageType = messageType;
    }

    @Override
    public MessageReaction payload(Object obj) {
        payload = obj;
        return this;
    }

    @Override
    public MessageReaction messageType(WsMessageType type) {
        messageType = type;
        return this;
    }

    public Object getPayload() {
        return payload;
    }

    public WsMessageType getMessageType() {
        return messageType;
    }
}