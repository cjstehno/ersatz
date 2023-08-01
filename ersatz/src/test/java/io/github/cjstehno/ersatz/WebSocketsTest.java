/**
 * Copyright (C) 2023 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.cfg.MessageType;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.testthings.rando.NumberRandomizers;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.junit.jupiter.api.Disabled;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ErsatzServerExtension.class)
public class WebSocketsTest {

    @Test
    void connecting(final ErsatzServer ersatz) {
        ersatz.expectations(expects -> {
            expects.webSocket("/stuff");
        });

        openWebSocket(ersatz.wsUrl("/stuff"));

        ersatz.assertVerified();
    }

    @Test @Disabled("fifth")
    void sendAndReceiveText(final ErsatzServer server) {
        val inboundMessageContent = "some message";

        server.expectations(expects -> {
            expects.webSocket("/ws", ws -> {
                ws.receive(inboundMessageContent, TEXT);
            });
        });

        openWebSocket(server.wsUrl("/ws"), wskt -> {
            wskt.send(inboundMessageContent);
        });

        server.assertVerified();
    }

    @Test @Disabled("second")
    void sendAndReceiveBinary(final ErsatzServer server) {
        val bytes = NumberRandomizers.byteArray(8).one();
        val messageContent = ByteString.of(bytes);

        server.expectations(expects -> {
            expects.webSocket("/ws", ws -> {
                ws.receive(bytes, BINARY);
            });
        });

        openWebSocket(server.wsUrl("/ws"), wskt -> {
            wskt.send(messageContent);
        });

        server.assertVerified();
    }

    @Test @Disabled("third")
    void multipleConnections(final ErsatzServer ersatz) {
        ersatz.expectations(expects -> {
            expects.webSocket("/alpha");
            expects.webSocket("/bravo");
        });

        openWebSocket(ersatz.wsUrl("/alpha"));
        openWebSocket(ersatz.wsUrl("/bravo"));

        ersatz.assertVerified();
    }

    @Test @Disabled("seventh")
    void reactToMessageWithText(final ErsatzServer ersatz) throws InterruptedException {
        ersatz.expectations(expects -> {
            expects.webSocket("/foo", ws -> {
                ws.receive("ping").reaction("pong", TEXT);
            });
        });

        val listener = new CapturingWebSocketListener(1);

        openWebSocket(ersatz.wsUrl("/foo"), listener, wskt -> {
            wskt.send("ping");
        });

        ersatz.assertVerified();

        listener.await(1, TimeUnit.SECONDS);
        assertEquals(listener.getMessages().get(0), "pong");
    }

    @Test @Disabled("sixth")
    void reactToMessageWithBinary(final ErsatzServer ersatz) throws Exception {
        val pingBytes = "ping".getBytes(UTF_8);
        val pingMessage = ByteString.of(pingBytes);
        val pongBytes = "pong".getBytes(UTF_8);
        val pongMessage = ByteString.of(pongBytes);

        ersatz.expectations(expects -> {
            expects.webSocket("/foo", ws -> {
                ws.receive(pingBytes).reaction(pongBytes, BINARY);
            });
        });

        val listener = new CapturingWebSocketListener(1);

        openWebSocket(ersatz.wsUrl("/foo"), listener, wskt -> {
            wskt.send(pingMessage);
        });

        ersatz.assertVerified();

        listener.await(1, TimeUnit.SECONDS);
        assertEquals(listener.getMessages().get(0), pongMessage);
    }

    @ParameterizedTest(name = "[{index}] sending message on connection: {0}") @MethodSource("onConnectMessages") @Disabled("first")
    void sendingMessageOnConnect(final MessageType mType, final Object message, final Object expected, final ErsatzServer ersatz) throws Exception {
        ersatz.expectations(expects -> {
            expects.webSocket("/hello", ws -> ws.send(message, mType));
        });

        val listener = new CapturingWebSocketListener(1);

        openWebSocket(ersatz.wsUrl("/hello"), listener, null);

        ersatz.assertVerified();

        listener.await(1, TimeUnit.SECONDS);
        assertEquals(listener.getMessages().get(0), expected);
    }

    private static Stream<Arguments> onConnectMessages() {
        return Stream.of(
            // type, message, expected
            Arguments.of(TEXT, "message for you, sir", "message for you, sir"),
            Arguments.of(BINARY, "message for you, sir".getBytes(UTF_8), ByteString.of("message for you, sir".getBytes(UTF_8)))
        );
    }

    // FIXME: pull the ws client into a reusable client extension
    private void openWebSocket(final String url) {
        openWebSocket(url, null);
    }

    private void openWebSocket(final String url, Consumer<WebSocket> consumer) {
        openWebSocket(url, null, consumer);
    }

    private void openWebSocket(final String url, WebSocketListener listener, Consumer<WebSocket> consumer) {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        WebSocket webSocket = new OkHttpClient.Builder().build().newWebSocket(request, listener != null ? listener : new CapturingWebSocketListener(0));

        if (consumer != null) {
            consumer.accept(webSocket);
        }

        webSocket.close(1000, "done");
    }

    @Slf4j
    private static class CapturingWebSocketListener extends WebSocketListener {

        @Getter private final List<Object> messages = new LinkedList<>();
        private final CountDownLatch latch;

        CapturingWebSocketListener(int expectedMessageCount) {
            latch = new CountDownLatch(expectedMessageCount);
        }

        boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }

        @Override public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            log.info("open");
        }

        @Override public void onMessage(WebSocket webSocket, String text) {
            log.info("message (string): {}", text);
            messages.add(text);
            latch.countDown();
        }

        @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
            log.info("message (bytes): {}", bytes);
            messages.add(bytes);
            latch.countDown();
        }

        @Override public void onClosing(WebSocket webSocket, int code, String reason) {
            log.info("closing");
        }

        @Override public void onClosed(WebSocket webSocket, int code, String reason) {
            log.info("closed");
        }

        @Override public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
            log.info("failure: {}", t.getMessage());
        }
    }
}

