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

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.server.UnderlyingServer;
import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.HttpTraceHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

/**
 * An <code>UnderlyingServer</code> implementation based on the Undertow server platform.
 */
@Slf4j
public class UndertowUnderlyingServer implements UnderlyingServer {

    private static final String LOCALHOST = "localhost";
    private static final int UNSPECIFIED_PORT = -1;
    private final ServerConfigImpl serverConfig;
    private Undertow server;
    private int actualHttpPort = UNSPECIFIED_PORT;
    private int actualHttpsPort = UNSPECIFIED_PORT;

    /**
     * Creates a new Undertow underlying server with the specified server config.
     *
     * @param serverConfig the server config information
     */
    public UndertowUnderlyingServer(final ServerConfigImpl serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override public void start() {
        if (server == null) {
            val builder = Undertow.builder()
                .addHttpListener(serverConfig.getDesiredHttpPort(), LOCALHOST)
                .setIoThreads(serverConfig.getIoThreads())
                .setWorkerThreads(serverConfig.getWorkerThreads());

            applyTimeout(builder, serverConfig.getTimeout());

            if (serverConfig.isHttpsEnabled()) {
                builder.addHttpsListener(serverConfig.getDesiredHttpsPort(), LOCALHOST, sslContext());
                log.debug("HTTPS listener enabled and configured.");
            }

            final var blockingHandler = new BlockingHandler(new EncodingHandler(
                new HttpTraceHandler(
                    new ErsatzHttpHandler(
                        serverConfig.getRequirements(),
                        serverConfig.getExpectations(),
                        serverConfig.isMismatchToConsole(),
                        serverConfig.isLogResponseContent()
                    )
                ),
                new ContentEncodingRepository().addEncodingHandler("gzip", new GzipEncodingProvider(), 50)
            ));

            server = builder.setHandler(blockingHandler).build();

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

    @Override public void stop() {
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
