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
package com.stehno.ersatz

import com.stehno.ersatz.cfg.WebSocketExpectations
import com.stehno.ersatz.cfg.WsMessageType
import com.stehno.ersatz.junit.ErsatzServerExtension
import com.stehno.ersatz.util.HttpClient
import groovy.util.logging.Slf4j
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.stream.Stream

import static com.stehno.ersatz.cfg.WsMessageType.BINARY
import static com.stehno.ersatz.cfg.WsMessageType.TEXT
import static java.nio.charset.StandardCharsets.UTF_8
import static java.util.concurrent.TimeUnit.SECONDS
import static okio.ByteString.of
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.params.provider.Arguments.arguments

@ExtendWith(ErsatzServerExtension) @Disabled("FIXME: there seems to be an issue")
class WebSocketsTest {

    private ErsatzServer ersatz
    private HttpClient client

    @BeforeEach void beforeEach() {
        client = new HttpClient()
    }

    @Test void 'specified ws block should expect at least one connect'() {
        ersatz.expectations {
            ws('/stuff')
        }

        openWebSocket("${ersatz.wsUrl}/stuff")

        assertTrue ersatz.verify()
    }

    @Test void 'specified multiple ws connection expectations'() {
        ersatz.expectations {
            ws('/ws')
            ws('/foo')
        }

        openWebSocket("${ersatz.wsUrl}/ws")
        openWebSocket("${ersatz.wsUrl}/foo")

        assertTrue ersatz.verify()
    }

    @ParameterizedTest @MethodSource('messagesProvider')
    void 'specify a ws block and expect a received message'(type, sentMessage, receivedMessage) {
        ersatz.expectations {
            ws('/ws') {
                receive(receivedMessage, type)
            }
        }

        openWebSocket("${ersatz.wsUrl}/ws") { WebSocket wskt ->
            wskt.send(sentMessage)
        }

        assertTrue ersatz.verify()
    }

    private static Stream<Arguments> messagesProvider() {
        Stream.of(
            arguments(TEXT, 'the message', 'the message'),
            arguments(BINARY, of('somebytes'.getBytes(UTF_8)), 'somebytes'.getBytes(UTF_8))
        )
    }

    @ParameterizedTest @MethodSource('messagesProvider')
    void 'specify a ws block and expect a received message (consumer: #type)'(type, sentMessage, receivedMessage) {
        def msg = receivedMessage
        def msgType = type

        ersatz.expectations {
            ws('/blah', new Consumer<WebSocketExpectations>() {
                @Override
                void accept(WebSocketExpectations ex) {
                    ex.receive(msg, msgType)
                }
            })
        }

        openWebSocket("${ersatz.wsUrl}/blah") { WebSocket wskt ->
            wskt.send(sentMessage)
        }

        assertTrue ersatz.verify()
    }

    @ParameterizedTest @MethodSource('messagesProvider')
    void 'specify a ws block and expect a received message (closure: type)'(WsMessageType type, sentMessage, receivedMessage) {
        ersatz.expectations {
            ws('/ws') {
                receive {
                    payload receivedMessage
                    messageType type
                }
            }
        }

        openWebSocket("${ersatz.wsUrl}/ws") { WebSocket wskt ->
            wskt.send(sentMessage)
        }

        assertTrue ersatz.verify()
    }

    // FIXME: same as above (remove)
    @Test
    void 'working'() {
        def type = TEXT
        def sentMessage = 'the message'
        def receivedMessage = 'the message'

        ersatz.expectations {
            ws('/ws') {
                receive {
                    payload receivedMessage
                    messageType type
                }
            }
        }

        openWebSocket("${ersatz.wsUrl}/ws") { WebSocket wskt ->
            wskt.send(sentMessage)
        }

        assertTrue ersatz.verify(1, SECONDS)
    }

    @ParameterizedTest @MethodSource('messagesProvider')
    void 'ws block expects a message and then reacts (method: #type)'(type, reactionMessage, clientMessage) {
        ersatz.expectations {
            ws('/ws') {
                receive('hello').reaction(reactionMessage, type)
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        openWebSocket("${ersatz.wsUrl}/ws", listener) { WebSocket wskt ->
            wskt.send('hello')
        }

        assertTrue ersatz.verify()

        assertTrue listener.await()
        assertEquals([clientMessage], listener.messages)
    }

    @ParameterizedTest @MethodSource('messagesProvider')
    void 'ws block expects a message and then reacts (closure: #type)'(type, reactionMessage, clientMessage) {
        ersatz.expectations {
            ws('/ws') {
                receive('hello').reaction {
                    payload reactionMessage
                    messageType type
                }
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        openWebSocket("${ersatz.wsUrl}/ws", listener) { WebSocket wskt ->
            wskt.send('hello')
        }

        assertTrue ersatz.verify()

        assertTrue listener.await()
        assertEquals([clientMessage], listener.messages)
    }

    @ParameterizedTest @MethodSource('messagesProvider')
    void 'ws block connects and sends message (method: #type)'(type, sentMessage, clientMessage) {
        ersatz.expectations {
            ws('/ws') {
                send(sentMessage, type)
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        openWebSocket "${ersatz.wsUrl}/ws", listener

        assertTrue ersatz.verify()

        assertTrue listener.await()
        assertEquals([clientMessage], listener.messages)
    }

    @ParameterizedTest @MethodSource('messagesProvider')
    void 'ws block connects and sends message (closure: #type)'(type, sentMessage, clientMessage) {
        ersatz.expectations {
            ws('/ws') {
                send {
                    payload sentMessage
                    messageType type
                }
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        openWebSocket "${ersatz.wsUrl}/ws", listener

        assertTrue ersatz.verify()

        assertTrue listener.await()
        assertEquals([clientMessage], listener.messages)
    }

    private void openWebSocket(String url, Closure closure = null) {
        client.openWebSocket(url, new CapturingWebSocketListener(0), closure)
    }
}

@Slf4j
class CapturingWebSocketListener extends WebSocketListener {

    final List messages = []
    private final CountDownLatch latch

    CapturingWebSocketListener(int expectedMessageCount) {
        latch = new CountDownLatch(expectedMessageCount)
    }

    boolean await(long timeout = 1, TimeUnit unit = SECONDS) {
        latch.await(timeout, unit)
    }

    @Override void onOpen(WebSocket webSocket, Response response) {
        log.info('open')
    }

    @Override void onMessage(WebSocket webSocket, String text) {
        log.info('message (string): {}', text)
        messages << text
        latch.countDown()
    }

    @Override void onMessage(WebSocket webSocket, ByteString bytes) {
        log.info('message (bytes): {}', bytes)
        messages << bytes
        latch.countDown()
    }

    @Override void onClosing(WebSocket webSocket, int code, String reason) {
        log.info('closing')
    }

    @Override void onClosed(WebSocket webSocket, int code, String reason) {
        log.info('closed')
    }

    @Override void onFailure(WebSocket webSocket, Throwable t, Response response) {
        log.info('failure: {}', t.message)
    }
}