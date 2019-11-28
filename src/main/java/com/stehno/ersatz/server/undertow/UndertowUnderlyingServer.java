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

import com.stehno.ersatz.*;
import com.stehno.ersatz.impl.*;
import com.stehno.ersatz.server.UnderlyingServer;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.HttpTraceHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.List;

import static com.stehno.ersatz.impl.ResponseChunker.prepareChunks;
import static io.undertow.util.HttpString.tryFromString;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

public class UndertowUnderlyingServer implements UnderlyingServer {

    private static final Logger log = LoggerFactory.getLogger(UndertowUnderlyingServer.class);
    private static final String LOCALHOST = "localhost";
    private static final String NO_HEADERS = "<no-headers>";
    private static final String EMPTY = "<empty>";
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
            serverConfig.getTimeoutConfig().accept(builder);

            if (serverConfig.isHttpsEnabled()) {
                builder.addHttpsListener(serverConfig.getDesiredHttpsPort(), LOCALHOST, sslContext());
            }

            BlockingHandler blockingHandler = new BlockingHandler(new EncodingHandler(
                applyAuthentication(
                    new HttpTraceHandler(
                        new HttpHandler() {
                            @Override
                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                                final ClientRequest clientRequest = new UndertowClientRequest(exchange);

                                log.debug("Request: {}", clientRequest);

                                serverConfig.getExpectations().findMatch(clientRequest)
                                    .ifPresentOrElse(
                                        req -> {
                                            final var ereq = (ErsatzRequest) req;
                                            send(exchange, ereq.getCurrentResponse());
                                            ereq.mark(clientRequest);
                                        },
                                        () -> {
                                            final var report = new UnmatchedRequestReport(clientRequest, serverConfig.getExpectations().getRequests().stream().map(r -> (ErsatzRequest) r).collect(toList()));

                                            log.warn(report.render());

                                            if (serverConfig.isMismatchToConsole()) {
                                                System.out.println(report);
                                            }

                                            exchange.setStatusCode(404).getResponseSender().send(NOT_FOUND_BODY);
                                        }
                                    );
                            }
                        }
                    )
                ),
                new ContentEncodingRepository()
                    .addEncodingHandler("gzip", new GzipEncodingProvider(), 50)
                    .addEncodingHandler("deflate", new DeflateEncodingProvider(), 50)
            ));

            final WebSocketsHandlerBuilder wsBuilder = new WebSocketsHandlerBuilder(serverConfig.getExpectations(), blockingHandler, serverConfig.isMismatchToConsole());

            server = builder.setHandler(wsBuilder.build()).build();
            server.start();

            applyPorts();
        }
    }

    @Override public void stop() {
        if (server != null) {
            actualHttpPort = UNSPECIFIED_PORT;
            actualHttpsPort = UNSPECIFIED_PORT;

            server.stop();

            server = null;
        }
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
            SimpleIdentityManager identityManager = new SimpleIdentityManager(authConfig.getUsername(), authConfig.getPassword());
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
        }

        return result;
    }

    private static void send(final HttpServerExchange exchange, final Response response) {
        if (response != null) {
            if (response.getDelay() > 0) {
                try {
                    MILLISECONDS.sleep(response.getDelay());
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            exchange.setStatusCode(response.getCode());

            response.getHeaders().forEach((k, v) -> {
                v.forEach(value -> {
                    exchange.getResponseHeaders().add(tryFromString(k), value);
                });
            });

            if (response.getChunkingConfig() != null) {
                exchange.getResponseHeaders().add(tryFromString("Transfer-encoding"), "chunked");
            }

            response.getCookies().forEach((k, v) -> {
                if (v instanceof Cookie) {
                    final Cookie ersatzCookie = (Cookie) v;
                    final var cookie = new CookieImpl(k, ersatzCookie.getValue());
                    cookie.setPath(ersatzCookie.getPath());
                    cookie.setDomain(ersatzCookie.getDomain());
                    cookie.setMaxAge(ersatzCookie.getMaxAge());
                    cookie.setSecure(ersatzCookie.isSecure());
                    cookie.setVersion(ersatzCookie.getVersion());
                    cookie.setHttpOnly(ersatzCookie.isHttpOnly());
                    cookie.setComment(ersatzCookie.getComment());
                    exchange.getResponseCookies().put(k, cookie);

                } else {
                    exchange.getResponseCookies().put(k, new CookieImpl(k, v.toString()));
                }
            });
        }

        final String responseContent = response != null ? response.getContent() : null;
        final String responsePreview = responseContent != null ? responseContent : EMPTY;
        final ChunkingConfig chunking = response != null ? response.getChunkingConfig() : null;
        final var responseHeaders = exchange.getResponseHeaders() != null ? exchange.getResponseHeaders() : NO_HEADERS;

        if (responseContent != null && chunking != null) {
            log.debug("Chunked-Response({}; {}): {}", responseHeaders, chunking, responsePreview);

            final List<String> chunks = prepareChunks(responseContent, chunking.getChunks());
            exchange.getResponseSender().send(chunks.remove(0), new ResponseChunker(chunks, chunking.getDelay()));

        } else {
            log.debug("Response({}): {}", responseHeaders, responsePreview);

            exchange.getResponseSender().send(responseContent);
        }
    }

    private void applyPorts() {
        actualHttpPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();

        if (serverConfig.isHttpsEnabled()) {
            actualHttpsPort = ((InetSocketAddress) server.getListenerInfo().get(1).getAddress()).getPort();
        }
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

        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
