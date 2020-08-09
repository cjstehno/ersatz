/**
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
package com.stehno.ersatz.impl;

import io.undertow.util.ImmediatePooled;
import io.undertow.websockets.core.BufferedBinaryMessage;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import static com.stehno.ersatz.cfg.WsMessageType.BINARY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.when;

class ReceivedMessageImplTest {

    @Test @DisplayName("matching binary message type")
    void matching_binary(){
        final var message = new ReceivedMessageImpl("a bunch of bytes and some more bytes".getBytes(UTF_8), BINARY);

        final var buffers = new ByteBuffer[]{
            ByteBuffer.wrap("a bunch of bytes".getBytes()),
            ByteBuffer.wrap(" and some more bytes".getBytes())
        };

        final var incomingMessage = Mockito.mock(BufferedBinaryMessage.class);
        when(incomingMessage.getData()).thenReturn(new ImmediatePooled<>(buffers));

        final var matches = message.matches(incomingMessage);
        Assertions.assertTrue(matches);
    }
}