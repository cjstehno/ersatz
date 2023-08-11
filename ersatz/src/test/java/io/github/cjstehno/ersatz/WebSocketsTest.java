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
package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.CapturingWebSocketListener;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import lombok.val;
import okio.ByteString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.cjstehno.ersatz.cfg.MessageType.BINARY;
import static io.github.cjstehno.ersatz.cfg.MessageType.TEXT;
import static io.github.cjstehno.ersatz.cfg.WaitFor.FOREVER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class WebSocketsTest {

    private static final String MESSAGE_STRING = "message for you, sir";
    private static final byte[] MESSAGE_BYTES = MESSAGE_STRING.getBytes(UTF_8);
    private static final ByteString MESSAGE_BYTESTRING = ByteString.of(MESSAGE_BYTES);

    @Test void connecting(final ErsatzServer ersatz, final Client http) {
        ersatz.expectations(expects -> {
            expects.webSocket("/stuff");
        });

        http.webSocket(ersatz.wsUrl("/stuff"), null, null);

        ersatz.assertVerified(FOREVER);
    }

    /**
     * A bit of a contrived test - basically it ensures that a test will fail if the connection expectation is not met.
     */
    @Test void expectingConnectThatNeverHappens(final ErsatzServer ersatz, final Client http) {
        ersatz.expectations(expects -> {
            expects.webSocket("/never");
        });

        http.webSocket(ersatz.wsUrl("/ever"), null, null);

        // it will timeout after 1 s
        assertFalse(ersatz.verify());
    }

    @Test void sendAndReceiveText(final ErsatzServer server, final Client http) {
        server.expectations(expects -> {
            expects.webSocket("/ws", ws -> {
                ws.receives(MESSAGE_STRING, TEXT);
            });
        });

        http.webSocket(server.wsUrl("/ws"), null, wskt -> {
            wskt.send(MESSAGE_STRING);
        });

        server.assertVerified(FOREVER);
    }

    @Test void sendAndReceiveBinary(final ErsatzServer server, final Client http) {
        server.expectations(expects -> {
            expects.webSocket("/ws", ws -> {
                ws.receives(MESSAGE_BYTES, BINARY);
            });
        });

        http.webSocket(server.wsUrl("/ws"), null, wskt -> {
            wskt.send(MESSAGE_BYTESTRING);
        });

        server.assertVerified(FOREVER);
    }

    @Test void multipleConnections(final ErsatzServer ersatz, final Client http) {
        ersatz.expectations(expects -> {
            expects.webSocket("/alpha");
            expects.webSocket("/bravo");
        });

        http.webSocket(ersatz.wsUrl("/alpha"), null, null);
        http.webSocket(ersatz.wsUrl("/bravo"), null, null);

        ersatz.assertVerified(FOREVER);
    }

    @Test void reactToMessageWithText(final ErsatzServer ersatz, final Client http) throws InterruptedException {
        ersatz.expectations(expects -> {
            expects.webSocket("/foo", ws -> {
                ws.receives("ping").reaction("pong", TEXT);
            });
        });

        val listener = new CapturingWebSocketListener(1);

        http.webSocket(ersatz.wsUrl("/foo"), listener, wskt -> {
            wskt.send("ping");
        });

        ersatz.assertVerified(FOREVER);

        listener.await(1, SECONDS);
        assertEquals(listener.getMessages().get(0), "pong");
    }

    @Test @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    void reactToMessageWithBinary(final ErsatzServer ersatz, final Client http) throws Exception {
        val pingBytes = "ping".getBytes(UTF_8);
        val pingMessage = ByteString.of(pingBytes);
        val pongBytes = "pong".getBytes(UTF_8);
        val pongMessage = ByteString.of(pongBytes);

        ersatz.expectations(expects -> {
            expects.webSocket("/foo", ws -> {
                ws.receives(pingBytes).reaction(pongBytes, BINARY);
            });
        });

        val listener = new CapturingWebSocketListener(1);

        http.webSocket(ersatz.wsUrl("/foo"), listener, wskt -> {
            wskt.send(pingMessage);
        });

        ersatz.assertVerified(FOREVER);

        listener.await(1, SECONDS);
        assertEquals(listener.getMessages().get(0), pongMessage);
    }

    @Test void sendingMessageOnConnect(final ErsatzServer ersatz, final Client http) throws Exception {
        ersatz.expectations(expects -> {
            expects.webSocket("/hello", ws -> ws.sends(MESSAGE_STRING, TEXT));
        });

        val listener = new CapturingWebSocketListener(1);

        http.webSocket(ersatz.wsUrl("/hello"), listener, null);

        ersatz.assertVerified(FOREVER);

        listener.await(1, SECONDS);
        assertEquals(listener.getMessages().get(0), MESSAGE_STRING);
    }
}
