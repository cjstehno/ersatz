package com.stehno.ersatz

import groovy.util.logging.Slf4j
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

import static com.stehno.ersatz.WsMessageType.TEXT

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
                receive(message, type)
            }
        }

        when:
        openWebSocket("${ersatz.wsUrl}/ws") { WebSocket wskt ->
            wskt.send(message)
        }

        then:
        ersatz.verify()

        where:
        type | message
        TEXT | 'the message'
    }

    @Unroll 'specify a ws block and expect a received message (closure)'() {
        setup:
        ersatz.expectations {
            ws('/ws') {
                receive {
                    payload message
                    messageType type
                }
            }
        }

        when:
        openWebSocket("${ersatz.wsUrl}/ws") { WebSocket wskt ->
            wskt.send(message)
        }

        then:
        ersatz.verify()

        where:
        type | message
        TEXT | 'the message'
    }

    private void openWebSocket(final String url, Closure closure = null) {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build()
        WebSocket webSocket = client.newWebSocket(request, new LoggingWebSocketListener())

        closure?.call(webSocket)

        webSocket.close(1000, 'done')
    }
}

@Slf4j
class LoggingWebSocketListener extends WebSocketListener {

    @Override void onOpen(WebSocket webSocket, okhttp3.Response response) {
        log.info('open')
    }

    @Override void onMessage(WebSocket webSocket, String text) {
        log.info('message (string): {}', text)
    }

    @Override void onMessage(WebSocket webSocket, ByteString bytes) {
        log.info('message (bytes): {}', bytes)
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