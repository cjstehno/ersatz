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

@SuppressWarnings('ConfusingMethodName')
interface WebSocketExpectations {

    ReceivedMessage receive(Object payload)

    ReceivedMessage receive(Object payload, WsMessageType messageType)

    ReceivedMessage receive(@DelegatesTo(ReceivedMessage) Closure closure)

    // FIXME: consumer versions

    SentMessage send(Object payload)

    SentMessage send(Object payload, WsMessageType messageType)

    SentMessage send(@DelegatesTo(SentMessage) Closure closure)
}

// FIXME: move this out

@SuppressWarnings('ConfusingMethodName')
interface SentMessage {

    SentMessage payload(Object obj)

    SentMessage messageType(WsMessageType type)
}

class SentMessageImpl implements SentMessage {

    Object payload
    WsMessageType messageType

    @Override
    SentMessage payload(Object obj) {
        payload = obj
        this
    }

    @Override
    SentMessage messageType(WsMessageType type) {
        messageType = type
        this
    }
}