/**
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
package com.stehno.ersatz.server.undertow;

import com.stehno.ersatz.ErsatzProxy;
import com.stehno.ersatz.impl.ProxyConfigImpl;
import com.stehno.ersatz.server.UnderlyingProxyServer;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class UndertowUnderlyingProxyServer implements UnderlyingProxyServer {

    private static final Logger log = LoggerFactory.getLogger(ErsatzProxy.class);
    private static final String LOCALHOST = "localhost";
    private static final int EPHEMERAL_PORT = 0;
    private static final int UNSPECIFIED_PORT = -1;
    private int actualHttpPort = UNSPECIFIED_PORT;
    private final ProxyConfigImpl config;
    private final URI targetUri;
    private Undertow server;

    public UndertowUnderlyingProxyServer(final ProxyConfigImpl config) {
        this.config = config;
        this.targetUri = config.getTargetUri();
    }

    @Override public void start() {
        if (server == null) {
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

                    if (!config.getExpectations().getMatchers().stream().anyMatch(m -> m.matches(clientRequest))) {
                        log.warn("No proxy match found for request: {}", clientRequest);
                    }

                    proxyHandler.handleRequest(exchange);
                }
            }));

            server = builder.build();
            server.start();

            actualHttpPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();
        }
    }

    @Override @SuppressWarnings("PMD.NullAssignment") public void stop() {
        if (server != null) {
            actualHttpPort = UNSPECIFIED_PORT;

            server.stop();
            server = null;
        }
    }

    @Override public String getUrl() {
        return "http://localhost:" + getActualPort();
    }

    @Override public int getActualPort() {
        return actualHttpPort;
    }
}
