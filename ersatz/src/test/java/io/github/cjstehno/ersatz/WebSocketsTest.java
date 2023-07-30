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

import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.cfg.MessageType.TEXT;

@ExtendWith(ErsatzServerExtension.class) @ApplyServerConfig
public class WebSocketsTest {

    // FIXME: more and better testing

    private static void serverConfig(final ServerConfig cfg){
        cfg.autoStart(false);
    }

    private OkHttpClient client = new OkHttpClient.Builder().build();

    @Test void connecting(final ErsatzServer ersatz) {
        ersatz.expectations(expects -> {
            expects.ws("/stuff");
        });
        ersatz.start();

        openWebSocket(ersatz.wsUrl("/stuff"));

        ersatz.assertVerified();
    }

    @Test void generalUse(final ErsatzServer server) throws Exception {
        val inboundMessageContent = "some message";

        server.expectations(expects -> {
            expects.ws("/ws", ws -> {
                ws.receive(inboundMessageContent, TEXT);
            });
        });

        openWebSocket(server.wsUrl("/ws"), wskt -> {
            wskt.send(inboundMessageContent);
        });

        server.assertVerified();
    }

    private void openWebSocket(final String url) {
        openWebSocket(url, null);
    }

    private void openWebSocket(final String url, Consumer<WebSocket> consumer) {
        openWebSocket(url, null, consumer);
    }

    private void openWebSocket(final String url, WebSocketListener listener, Consumer<WebSocket> consumer) {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        WebSocket webSocket = client.newWebSocket(request, listener != null ? listener : new CapturingWebSocketListener(0));

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

