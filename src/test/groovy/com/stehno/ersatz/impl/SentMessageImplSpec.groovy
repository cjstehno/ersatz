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

import spock.lang.Specification

import static com.stehno.ersatz.cfg.WsMessageType.BINARY
import static com.stehno.ersatz.cfg.WsMessageType.TEXT

class SentMessageImplSpec extends Specification {

    def 'methods'(){
        setup:
        SentMessageImpl message = new SentMessageImpl()

        when:
        message.payload('stuff')
        message.messageType(BINARY)

        then:
        message.payload == 'stuff'
        message.messageType == BINARY
    }

    def 'builder'(){
        when:
        SentMessageImpl message = new SentMessageImpl().payload('builder').messageType(TEXT)

        then:
        message.payload == 'builder'
        message.messageType == TEXT
    }
}
