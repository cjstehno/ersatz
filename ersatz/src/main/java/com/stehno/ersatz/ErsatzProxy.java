/**
 * Copyright (C) 2020 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz;

import com.stehno.ersatz.cfg.ProxyConfig;
import com.stehno.ersatz.impl.ProxyConfigImpl;
import com.stehno.ersatz.server.UnderlyingProxyServer;
import com.stehno.ersatz.server.undertow.UndertowUnderlyingProxyServer;

import java.util.function.Consumer;

/**
 * Standalone simple proxy server useful for testing proxy configurations.
 * <p>
 * At this time, only HTTP proxies are supported.
 */
public class ErsatzProxy {

    private final UnderlyingProxyServer server;
    private ProxyConfigImpl config = new ProxyConfigImpl();

    /**
     * Creates a new proxy server with the specified configuration. The configuration consumer will be provided an instance of
     * <code>ProxyConfig</code> for the actual configuration.
     * <p>
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param consumer the configuration consumer
     */
    public ErsatzProxy(final Consumer<ProxyConfig> consumer) {
        this(new ProxyConfigImpl(), consumer);
    }

    protected ErsatzProxy(final ProxyConfigImpl proxyConfig, final Consumer<ProxyConfig> consumer) {
        this.config = proxyConfig;
        this.server = new UndertowUnderlyingProxyServer(config);

        consumer.accept(config);

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
    public final void start() {
        server.start();
    }

    /**
     * Used to verify that the expected requests were proxied.
     *
     * @return a value of true if the expected requests were proxied
     */
    public boolean verify() {
        for (final var matcher : config.getExpectations().getMatchers()) {
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
        return server.getUrl();
    }

    /**
     * Used to retrieve the actual port of the HTTP proxy server.
     *
     * @return the port of the server
     */
    public int getPort() {
        return server.getActualPort();
    }

    /**
     * Used to stop the proxy server, if it was running.
     */
    public void stop() {
        server.stop();
    }
}
