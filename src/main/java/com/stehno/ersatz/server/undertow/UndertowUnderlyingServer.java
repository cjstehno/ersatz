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

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.impl.ServerConfigImpl;
import com.stehno.ersatz.server.UnderlyingServer;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.HttpTraceHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import static io.undertow.UndertowOptions.*;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class UndertowUnderlyingServer implements UnderlyingServer {

    private static final Logger log = LoggerFactory.getLogger(UndertowUnderlyingServer.class);
    private static final String LOCALHOST = "localhost";
    private static final int UNSPECIFIED_PORT = -1;
    private final ServerConfigImpl serverConfig;
    private Undertow server;
    private int actualHttpPort = UNSPECIFIED_PORT;
    private int actualHttpsPort = UNSPECIFIED_PORT;

    public UndertowUnderlyingServer(final ServerConfigImpl serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override public void start() {
        if (server == null) {
            final Undertow.Builder builder = Undertow.builder().addHttpListener(serverConfig.getDesiredHttpPort(), LOCALHOST);
            applyTimeout(builder, serverConfig.getTimeout());

            if (serverConfig.isHttpsEnabled()) {
                builder.addHttpsListener(serverConfig.getDesiredHttpsPort(), LOCALHOST, sslContext());
                log.debug("HTTPS listener enabled and configured.");
            }

            final var blockingHandler = new BlockingHandler(new EncodingHandler(
                applyAuthentication(
                    new HttpTraceHandler(
                        new ErsatzHttpHandler(serverConfig.getExpectations(), serverConfig.isMismatchToConsole())
                    )
                ),
                new ContentEncodingRepository()
                    .addEncodingHandler("gzip", new GzipEncodingProvider(), 50)
                    .addEncodingHandler("deflate", new DeflateEncodingProvider(), 50)
            ));

            server = builder.setHandler(
                new WebSocketsHandlerBuilder(serverConfig.getExpectations(), blockingHandler, serverConfig.isMismatchToConsole()).build()
            ).build();

            server.start();

            applyPorts();

            log.info("Started.");
        }
    }

    private void applyTimeout(final Undertow.Builder builder, final long timeout) {
        if (timeout > 0) {
            final var ms = (int) timeout;
            builder.setServerOption(IDLE_TIMEOUT, ms);
            builder.setServerOption(NO_REQUEST_TIMEOUT, ms);
            builder.setServerOption(REQUEST_PARSE_TIMEOUT, ms);
            builder.setSocketOption(Options.READ_TIMEOUT, ms);
            builder.setSocketOption(Options.WRITE_TIMEOUT, ms);

            log.debug("Timeout ({} ms) applied.", timeout);
        }
    }

    @Override @SuppressWarnings("PMD.NullAssignment") public void stop() {
        if (server != null) {
            actualHttpPort = UNSPECIFIED_PORT;
            actualHttpsPort = UNSPECIFIED_PORT;

            server.stop();

            server = null;
        }

        log.info("Stopped.");
    }

    @Override public int getActualHttpPort() {
        return actualHttpPort;
    }

    @Override public int getActualHttpsPort() {
        return actualHttpsPort;
    }

    private HttpHandler applyAuthentication(final HttpHandler handler) {
        HttpHandler result = handler;

        final var authConfig = serverConfig.getAuthenticationConfig();
        if (authConfig != null) {
            final var identityManager = new SimpleIdentityManager(authConfig.getUsername(), authConfig.getPassword());
            switch (authConfig.getType()) {
                case BASIC:
                    result = new BasicAuthHandler(identityManager).apply(result);
                    break;
                case DIGEST:
                    result = new DigestAuthHandler(identityManager).apply(result);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid authentication configuration.");
            }

            log.debug("Applied {} authentication (username:{}, password:{}).", authConfig.getType(), authConfig.getUsername(), authConfig.getPassword());
        }

        return result;
    }

    private void applyPorts() {
        actualHttpPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();

        if (serverConfig.isHttpsEnabled()) {
            actualHttpsPort = ((InetSocketAddress) server.getListenerInfo().get(1).getAddress()).getPort();
        }

        log.debug("Applied ports (http:{}, https:{}).", actualHttpPort, actualHttpsPort);
    }

    private SSLContext sslContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            final var location = serverConfig.getKeystoreLocation() != null ? serverConfig.getKeystoreLocation() : ErsatzServer.class.getResource("/ersatz.keystore");

            try (final InputStream instr = location.openStream()) {
                keyStore.load(instr, serverConfig.getKeystorePass().toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, serverConfig.getKeystorePass().toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext;

        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
