/*
 * Copyright (C) 2018 Christopher J. Stehno
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

import static com.stehno.ersatz.WsMessageType.BINARY
import static com.stehno.ersatz.WsMessageType.TEXT
import static java.util.concurrent.TimeUnit.SECONDS

class WebSocketsSpec extends Specification {

    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()

    @AutoCleanup('stop') private final ErsatzServer ersatz = new ErsatzServer()

    def 'specified ws block should expect at least one connect'() {
        setup:
        ersatz.expectations {
            ws('/ws')
        }

        when:
        openWebSocket("${ersatz.wsUrl}/ws")

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
        type   | sentMessage                      || receivedMessage
        TEXT   | 'the message'                    || 'the message'
        BINARY | ByteString.of('somebytes'.bytes) || 'somebytes'.bytes
    }

    @Unroll 'specify a ws block and expect a received message (closure)'() {
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
        type   | sentMessage                      || receivedMessage
        TEXT   | 'the message'                    || 'the message'
        BINARY | ByteString.of('somebytes'.bytes) || 'somebytes'.bytes
    }

    def 'ws block expects a message and then reacts (method)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                receive('hello').reaction('howdy')
            }
        }

        CapturingWebSocketListener listener = new CapturingWebSocketListener(1)

        when:
        openWebSocket("${ersatz.wsUrl}/ws", listener) { WebSocket wskt ->
            wskt.send('hello')
        }

        // TODO: test closure version and both binary and text.

        then:
        ersatz.verify()

        and:
        listener.await()
        listener.textMessages == ['howdy']
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

    final List<String> textMessages = []
    final List<ByteString> binaryMessages = []
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
        textMessages << text
        latch.countDown()
    }

    @Override void onMessage(WebSocket webSocket, ByteString bytes) {
        log.info('message (bytes): {}', bytes)
        binaryMessages << bytes
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