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
package com.stehno.ersatz;

import com.stehno.ersatz.cfg.ProxyConfig;
import com.stehno.ersatz.cfg.impl.ProxyConfigImpl;
import com.stehno.ersatz.match.ProxyRequestMatcher;
import com.stehno.ersatz.server.undertow.UndertowClientRequest;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Standalone simple proxy server useful for testing proxy configurations.
 * <p>
 * At this time, only HTTP proxies are supported.
 */
public class ErsatzProxy {

    // TODO: ensure that the packaging of this library conforms

    private static final Logger log = LoggerFactory.getLogger(ErsatzProxy.class);
    private static final String LOCALHOST = "localhost";
    private static final int EPHEMERAL_PORT = 0;
    private static final int UNSPECIFIED_PORT = -1;

    private final List<ProxyRequestMatcher> matchers = new LinkedList<>();
    private final URI targetUri;
    private int actualHttpPort = UNSPECIFIED_PORT;
    private Undertow server;
    private boolean started;

    /**
     * Creates a new proxy server with the specified configuration. The configuration closure will delegate to an instance of <code>ProxyConfig</code>
     * for the actual configuration.
     * <p>
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param closure the configuration closure.
     */
    public ErsatzProxy(@DelegatesTo(value = ProxyConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        ProxyConfigImpl config = new ProxyConfigImpl();
        ConsumerWithDelegate.create(closure).accept(config);

        targetUri = config.getTargetUri();
        matchers.addAll(config.getExpectations().getMatchers());

        if (config.isAutoStart()) {
            start();
        }
    }

    /**
     * Creates a new proxy server with the specified configuration. The configuration consumer will be provided an instance of
     * <code>ProxyConfig</code> for the actual configuration.
     * <p>
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param consumer the configuration consumer
     */
    public ErsatzProxy(final Consumer<ProxyConfig> consumer) {
        ProxyConfigImpl config = new ProxyConfigImpl();
        consumer.accept(config);

        targetUri = config.getTargetUri();
        matchers.addAll(config.getExpectations().getMatchers());

        if (config.isAutoStart()) {
            start();
        }
    }

    /**
     * Starts the proxy server on an ephemeral port on localhost, so that it does not collide with other running servers.
     * Once the server is started (when this method returns) the actual port and url will be available via the server getter methods.
     * <p>
     * This method does not need to be called if auto-start is not disabled.
     */
    public void start() {
        if (!started) {
            final Undertow.Builder builder = Undertow.builder().addHttpListener(EPHEMERAL_PORT, LOCALHOST);

            final LoadBalancingProxyClient client = new LoadBalancingProxyClient();
            client.setConnectionsPerThread(1);
            client.setMaxQueueSize(10);
            client.setProblemServerRetry(3);
            client.setSoftMaxConnectionsPerThread(1);
            client.setTtl(1000);
            client.addHost(targetUri);

            final var proxyHandler = ProxyHandler.builder().setProxyClient(client).build();

            builder.setHandler(new BlockingHandler(new HttpHandler() {
                @Override public void handleRequest(final HttpServerExchange exchange) throws Exception {
                    final var clientRequest = new UndertowClientRequest(exchange);

                    final var matched = matchers.stream().anyMatch(m -> m.matches(clientRequest));
                    if (!matched) {
                        log.warn("No proxy match found for request: {}", clientRequest);
                    }

                    proxyHandler.handleRequest(exchange);
                }
            }));

            server = builder.build();
            server.start();

            actualHttpPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();

            started = true;
        }
    }

    /**
     * Used to verify that the expected requests were proxied.
     *
     * @return a value of true if the expected requests were proxied
     */
    public boolean verify() {
        for (final ProxyRequestMatcher matcher : matchers) {
            if (matcher.getMatchCount() <= 0) {
                throw new IllegalArgumentException("Expected requests were not matched.");
            }
        }
        return true;
    }

    /**
     * Used to retrieve the full URL of the HTTP proxy server.
     *
     * @return the full URL of the HTTP server
     */
    public String getUrl() {
        return "http://localhost:" + getPort();
    }

    /**
     * Used to retrieve the actual port of the HTTP proxy server.
     *
     * @return the port of the server
     */
    public int getPort() {
        return actualHttpPort;
    }

    /**
     * Used to stop the proxy server, if it was running.
     */
    public void stop() {
        if (started) {
            actualHttpPort = UNSPECIFIED_PORT;

            if (server != null) {
                server.stop();
            }

            started = false;
        }
    }
}
