/*
 * Copyright (C) 2018 Christopher J. Stehno
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
package com.stehno.ersatz

/**
 * FIXME: document
 */
@SuppressWarnings('ConfusingMethodName')
interface ReceivedMessage {

    // generally a string or byte array
    ReceivedMessage payload(Object obj)

    ReceivedMessage messageType(WsMessageType type)

    MessageReaction reaction(Object payload)

    MessageReaction reaction(Object payload, WsMessageType messageType)

    MessageReaction reaction(@DelegatesTo(MessageReaction) Closure closure)
}

// FIXME: move these out

@SuppressWarnings('ConfusingMethodName')
interface MessageReaction {

    MessageReaction payload(Object obj)

    MessageReaction messageType(WsMessageType type)
}

class MessageReactionImpl implements MessageReaction {

    Object payload
    WsMessageType messageType

    @Override
    MessageReaction payload(Object obj) {
        payload = obj
        this
    }

    @Override
    MessageReaction messageType(WsMessageType type) {
        messageType = type
        this
    }
}