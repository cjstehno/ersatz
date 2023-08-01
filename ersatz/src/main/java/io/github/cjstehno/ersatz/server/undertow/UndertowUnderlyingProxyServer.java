/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.server.undertow;

import io.github.cjstehno.ersatz.impl.ProxyServerConfigImpl;
import io.github.cjstehno.ersatz.server.UnderlyingProxyServer;
import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * The Undertow-based implementation of the <code>UnderlyingProxyServer</code> interface.
 */
@Slf4j @RequiredArgsConstructor
public class UndertowUnderlyingProxyServer implements UnderlyingProxyServer {

    private static final String LOCALHOST = "localhost";
    private static final int EPHEMERAL_PORT = 0;
    private static final int UNSPECIFIED_PORT = -1;
    private final ProxyServerConfigImpl serverConfig;
    private int actualHttpPort = UNSPECIFIED_PORT;
    private Undertow server;

    @Override public void start() {
        if (server == null) {
            val builder = Undertow.builder()
                .addHttpListener(EPHEMERAL_PORT, LOCALHOST)
                .setIoThreads(serverConfig.getIoThreads())
                .setWorkerThreads(serverConfig.getWorkerThreads());

            val proxyClient = new LoadBalancingProxyClient()
                .setConnectionsPerThread(1)
                .setMaxQueueSize(10)
                .setProblemServerRetry(3)
                .setSoftMaxConnectionsPerThread(1)
                .setTtl(1000)
                .addHost(serverConfig.getTargetUri());

            val proxyHandler = ProxyHandler.builder().setProxyClient(proxyClient).build();

            builder.setHandler(new BlockingHandler(exchange -> {
                val clientRequest = new UndertowClientRequest(exchange);

                val matched = serverConfig.getExpectations().matches(clientRequest);
                if (!matched) {
                    log.warn("No proxy match found for request: {}", clientRequest);
                }

                proxyHandler.handleRequest(exchange);
            }));

            server = builder.build();
            server.start();

            actualHttpPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();
        }
    }

    @Override public void stop() {
        if (server != null) {
            actualHttpPort = UNSPECIFIED_PORT;

            server.stop();
            server = null;
        }
    }

    @Override public int getActualHttpPort() {
        return actualHttpPort;
    }

}
