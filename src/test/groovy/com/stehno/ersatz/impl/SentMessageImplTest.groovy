/*
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
package com.stehno.ersatz.impl


import org.junit.jupiter.api.Test

import static com.stehno.ersatz.cfg.WsMessageType.BINARY
import static com.stehno.ersatz.cfg.WsMessageType.TEXT
import static org.junit.jupiter.api.Assertions.assertEquals

class SentMessageImplTest {

    @Test void methods() {
        SentMessageImpl message = new SentMessageImpl()

        message.payload('stuff')
        message.messageType(BINARY)

        then:
        assertEquals 'stuff', message.payload
        assertEquals BINARY, message.messageType
    }

    @Test void builder() {
        SentMessageImpl message = new SentMessageImpl().payload('builder').messageType(TEXT)

        assertEquals 'builder', message.payload
        assertEquals TEXT, message.messageType
    }
}
