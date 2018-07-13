package com.stehno.ersatz

import groovy.util.logging.Slf4j
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

@Slf4j
class WebSocketClient {

    static void main(args){
        OkHttpClient client = new OkHttpClient.Builder().build()

        okhttp3.Request request = new okhttp3.Request.Builder().url('ws://localhost:8080/something').build()

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener(){
            @Override
            void onOpen(WebSocket webSocket, okhttp3.Response response) {
                log.info('open')
            }

            @Override
            void onMessage(WebSocket webSocket, String text) {
                log.info('message (string): {}', text)
            }

            @Override
            void onMessage(WebSocket webSocket, ByteString bytes) {
                log.info('message (bytes): {}', bytes)
            }

            @Override
            void onClosing(WebSocket webSocket, int code, String reason) {
                log.info('closing')
            }

            @Override
            void onClosed(WebSocket webSocket, int code, String reason) {
                log.info('closed')
            }

            @Override
            void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                log.info('failure')
            }
        })

        webSocket.send('Hello web sockets!')
        webSocket.send('You still there')

//        sleep 1000
//
//        webSocket.close(0, 'done')
    }
}
