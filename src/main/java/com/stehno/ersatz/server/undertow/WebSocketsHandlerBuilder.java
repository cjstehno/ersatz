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
package com.stehno.ersatz.server.undertow;

import com.stehno.ersatz.cfg.WsMessageType;
import com.stehno.ersatz.cfg.impl.ExpectationsImpl;
import com.stehno.ersatz.cfg.impl.ReceivedMessageImpl;
import com.stehno.ersatz.impl.UnmatchedWsReport;
import com.stehno.ersatz.cfg.impl.WebSocketExpectationsImpl;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Used to build the HttpHandler chain for web socket support.
 */
public class WebSocketsHandlerBuilder {

    private static final Logger log = LoggerFactory.getLogger(WebSocketsHandlerBuilder.class);
    private final ExpectationsImpl expectations;
    private final HttpHandler defaultHandler;
    private final boolean mismatchToConsole;

    public WebSocketsHandlerBuilder(ExpectationsImpl expectations, HttpHandler defaultHandler, boolean mismatchToConsole) {
        this.expectations = expectations;
        this.defaultHandler = defaultHandler;
        this.mismatchToConsole = mismatchToConsole;
    }

    public HttpHandler build() {
        HttpHandler wrappedHandler = defaultHandler;
        for (String path : expectations.getWebSocketPaths()) {
            wrappedHandler = buildPath(path, wrappedHandler);
        }
        return wrappedHandler;
    }

    private PathHandler buildPath(final String pathPrefix, final HttpHandler wrappedHandler) {
        return Handlers.path(wrappedHandler).addPrefixPath(pathPrefix, Handlers.websocket(new WebSocketConnectionCallback() {
            @Override
            public void onConnect(final WebSocketHttpExchange exchange, final WebSocketChannel channel) {
                log.debug("Connected ({}).", pathPrefix);

                // find the ws for this path and register a connection
                WebSocketExpectationsImpl wsExpectation = (WebSocketExpectationsImpl) expectations.findWsMatch(pathPrefix);
                if (wsExpectation != null) {
                    wsExpectation.connect();

                    // perform on-connect sends
                    wsExpectation.eachSender( sm -> WebSocketsHandlerBuilder.sendMessage(channel, sm.getPayload(), sm.getMessageType()));

                    channel.getReceiveSetter().set(new AbstractReceiveListener() {
                        @Override
                        protected void onFullTextMessage(WebSocketChannel ch, BufferedTextMessage message) throws IOException {
                            handleMessage(wsExpectation, ch, message);
                        }

                        @Override
                        protected void onFullBinaryMessage(WebSocketChannel ch, BufferedBinaryMessage message) throws IOException {
                            handleMessage(wsExpectation, ch, message);
                        }
                    });
                    channel.resumeReceives();

                } else {
                    throw new IllegalArgumentException("Web socket expectation was never connected.");
                }
            }
        }));
    }

    private void handleMessage(final WebSocketExpectationsImpl wsExpectation, final WebSocketChannel ch, final Object message) {
        wsExpectation.findMatch(message).ifPresentOrElse(
            expect -> {
                expect.mark();
                performReactions(expect, ch);
            },
            () -> {
                log.warn("Received ({}) message that has no configured expectation: {}", WsMessageType.resolve(message), message);

                final var report = new UnmatchedWsReport(wsExpectation);
                log.warn(report.render());

                if (mismatchToConsole) {
                    System.out.println(report);
                }
            }
        );
    }

    private static void performReactions(final ReceivedMessageImpl expectation, WebSocketChannel ch) {
        expectation.getReactions().forEach(reaction -> sendMessage(ch, reaction.getPayload(), reaction.getMessageType()));
    }

    private static void sendMessage(final WebSocketChannel ch, final Object payload, final WsMessageType messageType) {
        if (messageType == WsMessageType.BINARY) {
            WebSockets.sendBinary(ByteBuffer.wrap((byte[]) payload), ch, null);
        } else {
            WebSockets.sendText(payload.toString(), ch, null);
        }
    }
}