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
package com.stehno.ersatz

import groovy.transform.CompileStatic
import io.undertow.Undertow
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient
import io.undertow.server.handlers.proxy.ProxyHandler

import static com.stehno.vanilla.Affirmations.affirm

/**
 * Standalone simple proxy server useful for testing proxy configurations.
 */
@CompileStatic
class ErsatzProxy {

    // FIXME: add this to user guide and features list

    // TODO: HTTPS seems like it should not be too hard to configure; however, I ran into issues and decided to hold off.

    private static final String LOCALHOST = 'localhost'
    private static final int EPHEMERAL_PORT = 0
    private static final int UNSPECIFIED_PORT = -1
    private static final String HTTP = 'http'
    private static final String ONLY_HTTP_MESSAGE = 'Only HTTP targets are supported at this time.'

    private final URI target
    private int actualHttpPort = UNSPECIFIED_PORT
    private Undertow server
    private boolean started

    /**
     * Creates a proxy server which sends proxies requests to the specified target URL (Note: currently only HTTP is supported).
     *
     * @param target the target url
     */
    ErsatzProxy(final String target) {
        affirm target.toLowerCase().startsWith('http://'), ONLY_HTTP_MESSAGE

        this.target = target.toURI()
    }

    /**
     * Creates a proxy server which sends proxies requests to the specified target URL (Note: currently only HTTP is supported).
     *
     * @param target the target url
     */
    ErsatzProxy(final URI target) {
        affirm target.scheme.equalsIgnoreCase(HTTP), ONLY_HTTP_MESSAGE

        this.target = target
    }

    /**
     * Creates a proxy server which sends proxies requests to the specified target URL (Note: currently only HTTP is supported).
     *
     * @param target the target url
     */
    ErsatzProxy(final URL target) {
        affirm target.protocol.equalsIgnoreCase(HTTP), ONLY_HTTP_MESSAGE

        this.target = target.toURI()
    }

    /**
     * Starts the proxy server on an ephemeral port on localhost, so that it does not collide with other running servers.
     * Once the server is started (when this method returns) the actual port and url will be available via the server getter methods.
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
            ).addHost(target)

            builder.handler = new ProxyHandler(client, null)

            server = builder.build()
            server.start()

            applyPorts()

            started = true
        }
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

    private void applyPorts() {
        actualHttpPort = (server.listenerInfo[0].address as InetSocketAddress).port
    }
}
