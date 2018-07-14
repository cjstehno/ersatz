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
