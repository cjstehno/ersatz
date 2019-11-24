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
package com.stehno.ersatz.impl

import com.stehno.ersatz.WsMessageType
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import io.undertow.Handlers
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.PathHandler
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.*
import io.undertow.websockets.spi.WebSocketHttpExchange

import java.nio.ByteBuffer

/**
 * Used to build the HttpHandler chain for web socket support.
 */
@CompileStatic @Slf4j @TupleConstructor
class WebSocketsHandlerBuilder {

    final ExpectationsImpl expectations
    final HttpHandler defaultHandler
    final boolean mismatchToConsole

    HttpHandler build() {
        Set<String> wsPaths = expectations.webSocketPaths

        HttpHandler wrappedHandler = defaultHandler
        for (String path : wsPaths) {
            wrappedHandler = buildPath(path, wrappedHandler)
        }

        wrappedHandler
    }

    private PathHandler buildPath(final String pathPrefix, final HttpHandler wrappedHandler) {
        Handlers.path(wrappedHandler).addPrefixPath(pathPrefix, Handlers.websocket(new WebSocketConnectionCallback() {
            @Override
            void onConnect(final WebSocketHttpExchange exchange, final WebSocketChannel channel) {
                log.debug 'Connected ({}).', pathPrefix

                // find the ws for this path and register a connection
                WebSocketExpectationsImpl wsExpectation = expectations.findWsMatch(pathPrefix) as WebSocketExpectationsImpl
                if (wsExpectation) {
                    wsExpectation.connect()

                    // perform on-connect sends
                    wsExpectation.eachSender { SentMessageImpl sm ->
                        //noinspection UnnecessaryQualifiedReference
                        WebSocketsHandlerBuilder.sendMessage channel, sm.payload, sm.messageType
                    }

                    channel.receiveSetter.set(new AbstractReceiveListener() {
                        @Override
                        protected void onFullTextMessage(WebSocketChannel ch, BufferedTextMessage message) throws IOException {
                            handleMessage(wsExpectation, ch, message)
                        }

                        @Override
                        protected void onFullBinaryMessage(WebSocketChannel ch, BufferedBinaryMessage message) throws IOException {
                            handleMessage(wsExpectation, ch, message)
                        }
                    })
                    channel.resumeReceives()

                } else {
                    throw new IllegalArgumentException('Web socket expectation was never connected.')
                }
            }
        }))
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    private void handleMessage(final WebSocketExpectationsImpl wsExpectation, final WebSocketChannel ch, final Object message) {
        ReceivedMessageImpl expectation = wsExpectation.findMatch(message)
        if (expectation) {
            expectation.mark()
            performReactions expectation, ch

        } else {
            log.warn 'Received ({}) message that has no configured expectation: {}', WsMessageType.resolve(message), message

            UnmatchedWsReport report = new UnmatchedWsReport(wsExpectation)

            log.warn report.toString()

            if (mismatchToConsole) {
                println report
            }
        }
    }

    private static void performReactions(final ReceivedMessageImpl expectation, WebSocketChannel ch) {
        expectation.reactions.each { MessageReactionImpl reaction ->
            sendMessage(ch, reaction.payload, reaction.messageType)
        }
    }

    private static void sendMessage(final WebSocketChannel ch, final Object payload, final WsMessageType messageType) {
        switch (messageType) {
            case WsMessageType.BINARY:
                WebSockets.sendBinary(ByteBuffer.wrap(payload as byte[]), ch, null)
                break
            default:
                WebSockets.sendText(payload.toString(), ch, null)
        }
    }
}