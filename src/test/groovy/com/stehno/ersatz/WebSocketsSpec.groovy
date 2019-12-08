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
package com.stehno.ersatz

import com.stehno.ersatz.cfg.WebSocketExpectations
import groovy.util.logging.Slf4j
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

import static com.stehno.ersatz.cfg.WsMessageType.BINARY
import static com.stehno.ersatz.cfg.WsMessageType.TEXT
import static java.nio.charset.StandardCharsets.UTF_8
import static java.util.concurrent.TimeUnit.SECONDS
import static okio.ByteString.of

class WebSocketsSpec extends Specification {

    private OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()

    @AutoCleanup private ErsatzServer ersatz = new ErsatzServer()

    def 'specified ws block should expect at least one connect'() {
        setup:
        ersatz.expectations {
            ws('/stuff')
        }

        when:
        openWebSocket("${ersatz.wsUrl}/stuff")

        then:
        ersatz.verify()
    }

    def 'specified multiple ws connection expectations'() {
        setup:
        ersatz.expectations {
            ws('/ws')
            ws('/foo')
        }

        when:
        openWebSocket("${ersatz.wsUrl}/ws")
        openWebSocket("${ersatz.wsUrl}/foo")

        then:
        ersatz.verify()
    }

    @Unroll 'specify a ws block and expect a received message (method: #type)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                receive(receivedMessage, type)
            }
        }

        when:
        openWebSocket("${ersatz.wsUrl}/ws") { WebSocket wskt ->
            wskt.send(sentMessage)
        }

        then:
        ersatz.verify()

        where:
        type   | sentMessage                     || receivedMessage
        TEXT   | 'the message'                   || 'the message'
        BINARY | of('somebytes'.getBytes(UTF_8)) || 'somebytes'.getBytes(UTF_8)
    }

    @Unroll 'specify a ws block and expect a received message (consumer: #type)'() {
        setup:
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

        when:
        openWebSocket("${ersatz.wsUrl}/blah") { WebSocket wskt ->
            wskt.send(sentMessage)
        }

        then:
        ersatz.verify()

        where:
        type   | sentMessage                     || receivedMessage
        TEXT   | 'the message'                   || 'the message'
        BINARY | of('somebytes'.getBytes(UTF_8)) || 'somebytes'.getBytes(UTF_8)
    }

    @Unroll 'specify a ws block and expect a received message (closure: #type)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                receive {
                    payload receivedMessage
                    messageType type
                }
            }
        }

        when:
        openWebSocket("${ersatz.wsUrl}/ws") { WebSocket wskt ->
            wskt.send(sentMessage)
        }

        then:
        ersatz.verify()

        where:
        type   | sentMessage                     || receivedMessage
        TEXT   | 'the message'                   || 'the message'
        BINARY | of('somebytes'.getBytes(UTF_8)) || 'somebytes'.getBytes(UTF_8)
    }

    @Unroll 'ws block expects a message and then reacts (method: #type)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                receive('hello').reaction(reactionMessage, type)
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        when:
        openWebSocket("${ersatz.wsUrl}/ws", listener) { WebSocket wskt ->
            wskt.send('hello')
        }

        then:
        ersatz.verify()

        and:
        listener.await()
        listener.messages == [clientMessage]

        where:
        type   | reactionMessage             || clientMessage
        TEXT   | 'the message'               || 'the message'
        BINARY | 'somebytes'.getBytes(UTF_8) || of('somebytes'.getBytes(UTF_8))
    }

    @Unroll 'ws block expects a message and then reacts (closure: #type)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                receive('hello').reaction {
                    payload reactionMessage
                    messageType type
                }
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        when:
        openWebSocket("${ersatz.wsUrl}/ws", listener) { WebSocket wskt ->
            wskt.send('hello')
        }

        then:
        ersatz.verify()

        and:
        listener.await()
        listener.messages == [clientMessage]

        where:
        type   | reactionMessage             || clientMessage
        TEXT   | 'the message'               || 'the message'
        BINARY | 'somebytes'.getBytes(UTF_8) || of('somebytes'.getBytes(UTF_8))
    }

    @Unroll 'ws block connects and sends message (method: #type)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                send(sentMessage, type)
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        when:
        openWebSocket "${ersatz.wsUrl}/ws", listener

        then:
        ersatz.verify()

        and:
        listener.await()
        listener.messages == [clientMessage]

        where:
        type   | sentMessage                            || clientMessage
        TEXT   | 'message for you, sir'                 || 'message for you, sir'
        BINARY | 'message for you, sir'.getBytes(UTF_8) || of('message for you, sir'.getBytes(UTF_8))
    }

    @Unroll 'ws block connects and sends message (closure: #type)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                send {
                    payload sentMessage
                    messageType type
                }
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        when:
        openWebSocket "${ersatz.wsUrl}/ws", listener

        then:
        ersatz.verify()

        and:
        listener.await()
        listener.messages == [clientMessage]

        where:
        type   | sentMessage                            || clientMessage
        TEXT   | 'message for you, sir'                 || 'message for you, sir'
        BINARY | 'message for you, sir'.getBytes(UTF_8) || of('message for you, sir'.getBytes(UTF_8))
    }

    private void openWebSocket(final String url, Closure closure = null) {
        openWebSocket(url, null, closure)
    }

    private void openWebSocket(final String url, WebSocketListener listener, Closure closure = null) {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build()
        WebSocket webSocket = client.newWebSocket(request, listener ?: new CapturingWebSocketListener(0))

        closure?.call(webSocket)

        webSocket.close(1000, 'done')
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

    @Override void onOpen(WebSocket webSocket, okhttp3.Response response) {
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

    @Override void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
        log.info('failure: {}', t.message)
    }
}