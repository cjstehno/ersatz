/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.proxy

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.impl.UndertowClientRequest
import com.stehno.ersatz.proxy.impl.ProxyConfigImpl
import com.stehno.ersatz.proxy.impl.ProxyRequestMatcher
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient
import io.undertow.server.handlers.proxy.ProxyHandler

import java.util.function.Consumer

/**
 * Standalone simple proxy server useful for testing proxy configurations.
 *
 * At this time, only HTTP proxies are supported.
 */
@CompileStatic @Slf4j
class ErsatzProxy {

    private static final String LOCALHOST = 'localhost'
    private static final int EPHEMERAL_PORT = 0
    private static final int UNSPECIFIED_PORT = -1

    private final List<ProxyRequestMatcher> matchers = []
    private final URI targetUri
    private int actualHttpPort = UNSPECIFIED_PORT
    private Undertow server
    private boolean started

    /**
     * Creates a new proxy server with the specified configuration. The configuration closure will delegate to an instance of <code>ProxyConfig</code>
     * for the actual configuration.
     *
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param closure the configuration closure.
     */
    ErsatzProxy(@DelegatesTo(ProxyConfig) final Closure closure) {
        ProxyConfigImpl config = new ProxyConfigImpl()
        closure.delegate = config
        closure.call()

        targetUri = config.targetUri
        matchers.addAll config.expectations.matchers

        if (config.autoStart) {
            start()
        }
    }

    /**
     * Creates a new proxy server with the specified configuration. The configuration consumer will be provided an instance of
     * <code>ProxyConfig</code> for the actual configuration.
     *
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param closure the configuration consumer
     */
    ErsatzProxy(final Consumer<ProxyConfig> consumer) {
        ProxyConfigImpl config = new ProxyConfigImpl()
        consumer.accept(config)

        targetUri = config.targetUri
        matchers.addAll config.expectations.matchers

        if (config.autoStart) {
            start()
        }
    }

    /**
     * Starts the proxy server on an ephemeral port on localhost, so that it does not collide with other running servers.
     * Once the server is started (when this method returns) the actual port and url will be available via the server getter methods.
     *
     * This method does not need to be called if auto-start is not disabled.
     */
    void start() {
        if (!started) {
            Undertow.Builder builder = Undertow.builder().addHttpListener(EPHEMERAL_PORT, LOCALHOST)

            LoadBalancingProxyClient client = new LoadBalancingProxyClient(
                connectionsPerThread: 1,
                maxQueueSize: 10,
                problemServerRetry: 3,
                softMaxConnectionsPerThread: 1,
                ttl: 1000
            ).addHost(targetUri)

            def proxyHandler = new ProxyHandler(client, null)

            builder.handler = new BlockingHandler(new HttpHandler() {
                @Override void handleRequest(HttpServerExchange exchange) throws Exception {
                    ClientRequest clientRequest = new UndertowClientRequest(exchange)

                    boolean matched = matchers.any { ProxyRequestMatcher m -> m.matches(clientRequest) }
                    if (!matched) {
                        log.warn 'No proxy match found for request: {}', clientRequest
                    }

                    proxyHandler.handleRequest(exchange)
                }
            })

            server = builder.build()
            server.start()

            actualHttpPort = (server.listenerInfo[0].address as InetSocketAddress).port

            started = true
        }
    }

    /**
     * Used to verify that the expected requests were proxied.
     *
     * @return a value of true if the expected requests were proxied
     */
    boolean verify() {
        matchers.each { ProxyRequestMatcher matcher ->
            assert matcher.matchCount > 0
        }
        true
    }

    /**
     * Used to retrieve the full URL of the HTTP proxy server.
     *
     * @return the full URL of the HTTP server
     */
    String getUrl() {
        "http://localhost:$actualHttpPort"
    }

    /**
     * Used to retrieve the actual port of the HTTP proxy server.
     *
     * @return the port of the server
     */
    int getPort() {
        actualHttpPort
    }

    /**
     * Used to stop the proxy server, if it was running.
     */
    void stop() {
        if (started) {
            actualHttpPort = UNSPECIFIED_PORT

            server?.stop()

            started = false
        }
    }
}
