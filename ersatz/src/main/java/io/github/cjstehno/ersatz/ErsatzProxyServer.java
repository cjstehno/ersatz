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
package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.cfg.ProxyServerConfig;
import io.github.cjstehno.ersatz.impl.ProxyServerConfigImpl;
import io.github.cjstehno.ersatz.impl.matchers.MatchCountingMatcher;
import io.github.cjstehno.ersatz.server.UnderlyingProxyServer;
import io.github.cjstehno.ersatz.server.undertow.UndertowUnderlyingProxyServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Standalone simple proxy server useful for testing proxy configurations.
 * <p>
 * At this time, only HTTP proxies are supported.
 */
@Slf4j
public class ErsatzProxyServer implements Closeable {

    private final ProxyServerConfigImpl proxyServerConfig;
    private final UnderlyingProxyServer underlyingServer;

    /**
     * Creates a new proxy server with the specified configuration. The configuration consumer will be provided an
     * instance of <code>ProxyServerConfig</code> for the actual configuration.
     * <p>
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param consumer the configuration consumer
     */
    public ErsatzProxyServer(final Consumer<ProxyServerConfig> consumer) {
        proxyServerConfig = new ProxyServerConfigImpl();
        consumer.accept(proxyServerConfig);

        underlyingServer = new UndertowUnderlyingProxyServer(proxyServerConfig);

        if (proxyServerConfig.isAutoStart()) {
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
        underlyingServer.start();
    }

    /**
     * Used to verify that the expected requests were proxied.
     *
     * @return a value of true if the expected requests were proxied
     */
    public boolean verify() {
        for (val matcher : proxyServerConfig.getExpectations().getMatchers()) {
            if (((MatchCountingMatcher) matcher).getMatchedCount() < 1) {
                // TODO: better reporting
                throw new IllegalArgumentException("Expected match count was not met.");
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
        return "http://localhost:" + underlyingServer.getActualHttpPort();
    }

    /**
     * Used to retrieve the actual port of the HTTP proxy server.
     *
     * @return the port of the server
     */
    public int getPort() {
        return underlyingServer.getActualHttpPort();
    }

    /**
     * Used to stop the proxy server, if it was running.
     */
    public void stop() {
        underlyingServer.stop();
    }

    /**
     * Closes the server (calls the <code>stop()</code> method.
     *
     * @throws IOException if there is a problem
     */
    @Override public void close() throws IOException {
        stop();
    }
}
