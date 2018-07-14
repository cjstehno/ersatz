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
import io.undertow.Undertow
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import io.undertow.websockets.spi.WebSocketHttpExchange

import static io.undertow.Handlers.*

@Slf4j
class WebSocketServer {

    private Undertow server
    String lastReceivedMessage

    static void main(final String[] args) {
        WebSocketServer webSocketServer = new WebSocketServer()
        webSocketServer.buildAndStartServer(8080, "localhost")
    }

    void buildAndStartServer(int port, String host) {
        server = Undertow.builder()
                .addListener(port, host)
                .setHandler(getWebSocketHandler())
                .build()
        server.start()
    }

    void stopServer() {
        if (server != null) {
            server.stop()
        }
    }

    private PathHandler getWebSocketHandler() {
        return path().addPrefixPath("/something", websocket(new WebSocketConnectionCallback() {
            @Override
            void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
//                log.info('attachment: {}', )
                log.info('request-headers: {}', exchange.requestHeaders)
                log.info('response-headers: {}', exchange.responseHeaders)
                log.info('request-scheme: {}', exchange.requestScheme)
                log.info('request-uri: {}', exchange.requestURI)
                log.info('query-string: {}', exchange.queryString)
                log.info('session: {}', exchange.session)
                log.info('request-parameters: {}', exchange.requestParameters)
                log.info('user-principal: {}', exchange.userPrincipal)

                WebSockets.sendText('you are connected', channel, null)

                channel.getReceiveSetter().set(new AbstractReceiveListener() {
                    @Override
                    protected void onFullTextMessage(WebSocketChannel cnl, BufferedTextMessage message) {
                        String data = message.getData()
                        lastReceivedMessage = data
                        log.info(">>>> Received data: " + data)
                        WebSockets.sendText("echo -> $data", cnl, null)
                    }
                })
                channel.resumeReceives()
            }
        }))
                .addPrefixPath("/", resource(new ClassPathResourceManager(WebSocketServer.class.getClassLoader(), WebSocketServer.class.getPackage()))
                .addWelcomeFiles("index.html"))
    }
}