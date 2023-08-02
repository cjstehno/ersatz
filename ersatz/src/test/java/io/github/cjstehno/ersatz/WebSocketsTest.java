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

import io.github.cjstehno.ersatz.cfg.MessageType;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.cfg.MessageType.BINARY;
import static io.github.cjstehno.ersatz.cfg.MessageType.TEXT;
import static io.github.cjstehno.ersatz.cfg.WaitFor.FOREVER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(ErsatzServerExtension.class)
public class WebSocketsTest {

    private static final String MESSAGE_STRING = "message for you, sir";
    private static final byte[] MESSAGE_BYTES = MESSAGE_STRING.getBytes(UTF_8);
    private static final ByteString MESSAGE_BYTESTRING = ByteString.of(MESSAGE_BYTES);

    @Test void connecting(final ErsatzServer ersatz) {
        ersatz.expectations(expects -> {
            expects.webSocket("/stuff");
        });

        openWebSocket(ersatz.wsUrl("/stuff"));

        ersatz.assertVerified(FOREVER);
    }

    /**
     * A bit of a contrived test - basically it ensures that a test will fail if the connection expectation is not met.
     */
    @Test void expectingConnectThatNeverHappens(final ErsatzServer ersatz) {
        ersatz.expectations(expects -> {
            expects.webSocket("/never");
        });

        openWebSocket(ersatz.wsUrl("/ever"));

        // it will timeout after 1 s
        assertFalse(ersatz.verify());
    }

    @Test void sendAndReceiveText(final ErsatzServer server) {
        server.expectations(expects -> {
            expects.webSocket("/ws", ws -> {
                ws.receives(MESSAGE_STRING, TEXT);
            });
        });

        openWebSocket(server.wsUrl("/ws"), wskt -> {
            wskt.send(MESSAGE_STRING);
        });

        server.assertVerified(FOREVER);
    }

    @Test void sendAndReceiveBinary(final ErsatzServer server) {
        server.expectations(expects -> {
            expects.webSocket("/ws", ws -> {
                ws.receives(MESSAGE_BYTES, BINARY);
            });
        });

        openWebSocket(server.wsUrl("/ws"), wskt -> {
            wskt.send(MESSAGE_BYTESTRING);
        });

        server.assertVerified(FOREVER);
    }

    @Test void multipleConnections(final ErsatzServer ersatz) {
        ersatz.expectations(expects -> {
            expects.webSocket("/alpha");
            expects.webSocket("/bravo");
        });

        openWebSocket(ersatz.wsUrl("/alpha"));
        openWebSocket(ersatz.wsUrl("/bravo"));

        ersatz.assertVerified(FOREVER);
    }

    @Test void reactToMessageWithText(final ErsatzServer ersatz) throws InterruptedException {
        ersatz.expectations(expects -> {
            expects.webSocket("/foo", ws -> {
                ws.receives("ping").reaction("pong", TEXT);
            });
        });

        val listener = new CapturingWebSocketListener(1);

        openWebSocket(ersatz.wsUrl("/foo"), listener, wskt -> {
            wskt.send("ping");
        });

        ersatz.assertVerified(FOREVER);

        listener.await(1, TimeUnit.SECONDS);
        assertEquals(listener.getMessages().get(0), "pong");
    }

    @Test @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    void reactToMessageWithBinary(final ErsatzServer ersatz) throws Exception {
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

        openWebSocket(ersatz.wsUrl("/foo"), listener, wskt -> {
            wskt.send(pingMessage);
        });

        ersatz.assertVerified(FOREVER);

        listener.await(1, TimeUnit.SECONDS);
        assertEquals(listener.getMessages().get(0), pongMessage);
    }

    @ParameterizedTest(name = "[{index}] sending message on connection: {0}") @MethodSource("onConnectMessages")
    void sendingMessageOnConnect(final MessageType mType, final Object message, final Object expected, final ErsatzServer ersatz) throws Exception {
        ersatz.expectations(expects -> {
            expects.webSocket("/hello", ws -> ws.sends(message, mType));
        });

        val listener = new CapturingWebSocketListener(1);

        openWebSocket(ersatz.wsUrl("/hello"), listener, null);

        ersatz.assertVerified(FOREVER);

        listener.await(1, TimeUnit.SECONDS);
        assertEquals(listener.getMessages().get(0), expected);
    }

    private static Stream<Arguments> onConnectMessages() {
        return Stream.of(
            // type, message, expected
            Arguments.of(TEXT, MESSAGE_STRING, MESSAGE_STRING),
            Arguments.of(BINARY, MESSAGE_BYTES, MESSAGE_BYTESTRING)
        );
    }

    // FIXME: pull the ws client into a reusable client extension
    private void openWebSocket(final String url) {
        openWebSocket(url, null);
    }

    private void openWebSocket(final String url, final Consumer<WebSocket> consumer) {
        openWebSocket(url, null, consumer);
    }

    private void openWebSocket(final String url, final WebSocketListener listener, final Consumer<WebSocket> consumer) {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        WebSocket webSocket = new OkHttpClient.Builder().build().newWebSocket(
            request,
            listener != null ? listener : new CapturingWebSocketListener(0)
        );

        if (consumer != null) {
            consumer.accept(webSocket);
        }

        webSocket.close(1000, "done");
    }

    @Slf4j
    private static class CapturingWebSocketListener extends WebSocketListener {

        @Getter private final List<Object> messages = new LinkedList<>();
        private final CountDownLatch latch;

        CapturingWebSocketListener(final int expectedMessageCount) {
            latch = new CountDownLatch(expectedMessageCount);
        }

        boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }

        @Override public void onOpen(final WebSocket webSocket, final okhttp3.Response response) {
            log.info("open");
        }

        @Override public void onMessage(final WebSocket webSocket, final String text) {
            log.info("message (string): {}", text);
            messages.add(text);
            latch.countDown();
        }

        @Override public void onMessage(final WebSocket webSocket, final ByteString bytes) {
            log.info("message (bytes): {}", bytes);
            messages.add(bytes);
            latch.countDown();
        }

        @Override public void onClosing(final WebSocket webSocket, final int code, final String reason) {
            log.info("closing");
        }

        @Override public void onClosed(final WebSocket webSocket, final int code, final String reason) {
            log.info("closed");
        }

        @Override public void onFailure(final WebSocket webSocket, final Throwable t, final okhttp3.Response response) {
            log.info("failure: {}", t.getMessage());
        }
    }
}
