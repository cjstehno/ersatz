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