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
package io.github.cjstehno.ersatz.server.undertow;

import io.github.cjstehno.ersatz.cfg.MessageType;
import io.github.cjstehno.ersatz.impl.ExpectationsImpl;
import io.github.cjstehno.ersatz.impl.InboundMessageImpl;
import io.github.cjstehno.ersatz.impl.UnmatchedWsReport;
import io.github.cjstehno.ersatz.impl.WebSocketExpectationsImpl;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.websockets.core.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.ByteBuffer;

import static io.github.cjstehno.ersatz.cfg.MessageType.BINARY;
import static io.github.cjstehno.ersatz.cfg.MessageType.resolve;

@RequiredArgsConstructor @Slf4j
public class WebSocketsHandlerBuilder {

    private final ExpectationsImpl expectations;
    private final HttpHandler defaultHandler;
    private final boolean mismatchToConsole;

    public HttpHandler build() {
        HttpHandler wrappedHandler = defaultHandler;
        for (val path : expectations.getWebSocketPaths()) {
            wrappedHandler = buildPath(path, wrappedHandler);
        }
        return wrappedHandler;
    }

    private PathHandler buildPath(final String pathPrefix, final HttpHandler wrappedHandler) {
        return Handlers.path(wrappedHandler).addPrefixPath(pathPrefix, Handlers.websocket((exchange, channel) -> {
            log.debug("Connected ({}).", pathPrefix);

            // find the ws for this path and register a connection
            WebSocketExpectationsImpl wsExpectation = (WebSocketExpectationsImpl) expectations.findWsMatch(pathPrefix);
            if (wsExpectation != null) {
                wsExpectation.connect();

                // perform on-connect sends
                wsExpectation.eachSender(sm -> WebSocketsHandlerBuilder.sendMessage(channel, sm.getPayload(), sm.getMessageType()));

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
        }));
    }

    private void handleMessage(final WebSocketExpectationsImpl wsExpectation, final WebSocketChannel ch, final Object message) {
        wsExpectation.findMatch(message).ifPresentOrElse(
            expect -> {
                expect.mark();
                performReactions(expect, ch);
            },
            () -> {
                log.warn("Received ({}) message that has no configured expectation: {}", resolve(message), message);

                final var report = new UnmatchedWsReport(wsExpectation);
                log.warn(report.render());

                if (mismatchToConsole) {
                    System.out.println(report);
                }
            }
        );
    }

    private static void performReactions(final InboundMessageImpl expectation, WebSocketChannel ch) {
        expectation.getReactions().forEach(reaction -> sendMessage(ch, reaction.getPayload(), reaction.getMessageType()));
    }

    private static void sendMessage(final WebSocketChannel ch, final Object payload, final MessageType messageType) {
        if (messageType == BINARY) {
            WebSockets.sendBinary(ByteBuffer.wrap((byte[]) payload), ch, null);
        } else {
            WebSockets.sendText(payload.toString(), ch, null);
        }
    }
}