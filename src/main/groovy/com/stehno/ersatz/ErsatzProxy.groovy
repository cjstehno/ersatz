package com.stehno.ersatz

import groovy.transform.CompileStatic
import io.undertow.Undertow
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient
import io.undertow.server.handlers.proxy.ProxyHandler

/**
 * Standalone simple proxy server useful for testing proxy configurations.
 */
@CompileStatic
class ErsatzProxy {

    String target

    private static final String LOCALHOST = 'localhost'
    private static final int EPHEMERAL_PORT = 0
    private static final int UNSPECIFIED_PORT = -1

    private int actualHttpPort = UNSPECIFIED_PORT
    private Undertow server
    private boolean started

    // TODO: HTTPS support?

    void start() {
        if (!started) {
            Undertow.Builder builder = Undertow.builder().addHttpListener(EPHEMERAL_PORT, LOCALHOST)

            builder.handler = new ProxyHandler(new LoadBalancingProxyClient(
                connectionsPerThread: 1,
                maxQueueSize: 10,
                problemServerRetry: 3,
                softMaxConnectionsPerThread: 1,
                ttl: 1000
            ).addHost(target.toURI()), null)

            server = builder.build()
            server.start()

            applyPorts()

            started = true
        }
    }

    /**
     * Used to retrieve the full URL of the HTTP server.
     *
     * @return the full URL of the HTTP server
     */
    String getHttpUrl() {
        "http://localhost:$actualHttpPort"
    }

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
