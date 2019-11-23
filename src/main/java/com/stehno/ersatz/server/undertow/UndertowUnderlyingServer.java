package com.stehno.ersatz.server.undertow;

import com.stehno.ersatz.*;
import com.stehno.ersatz.auth.BasicAuthHandler;
import com.stehno.ersatz.auth.DigestAuthHandler;
import com.stehno.ersatz.auth.SimpleIdentityManager;
import com.stehno.ersatz.impl.*;
import com.stehno.ersatz.server.ServerConfigImpl;
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
import java.net.URL;
import java.security.KeyStore;
import java.util.List;

import static com.stehno.ersatz.impl.ResponseChunker.prepareChunks;
import static io.undertow.util.HttpString.tryFromString;
import static java.util.stream.Collectors.toList;

public class UndertowUnderlyingServer implements UnderlyingServer {

    private static final Logger log = LoggerFactory.getLogger(UndertowUnderlyingServer.class);
    private static final String NOT_FOUND_BODY = "404: Not Found";
    private static final String LOCALHOST = "localhost";
    private static final String NO_HEADERS = "<no-headers>";
    private static final String EMPTY = "<empty>";
    private static final int UNSPECIFIED_PORT = -1;

    private boolean started;
    private ServerConfigImpl serverConfig;
    private Undertow server;
    private int actualHttpPort = UNSPECIFIED_PORT;
    private int actualHttpsPort = UNSPECIFIED_PORT;

    public UndertowUnderlyingServer(final ServerConfigImpl serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override public void start() throws Exception {
        if (!started) {
            final Undertow.Builder builder = Undertow.builder().addHttpListener(serverConfig.getConfiguredHttpPort(), LOCALHOST);
            serverConfig.getTimeoutConfig().accept(builder);

            if (serverConfig.isHttpsEnabled()) {
                builder.addHttpsListener(serverConfig.getConfiguredHttpsPort(), LOCALHOST, sslContext());
            }

            final BlockingHandler blockingHandler = new BlockingHandler(new EncodingHandler(
                applyAuthentication(
                    new HttpTraceHandler(
                        new HttpHandler() {
                            @Override
                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                                final ClientRequest clientRequest = new UndertowClientRequest(exchange);

                                log.debug("Request: {}", clientRequest);

                                final ErsatzRequest request = (ErsatzRequest) serverConfig.getExpectations().findMatch(clientRequest);
                                if (request != null) {
                                    final Response currentResponse = request.getCurrentResponse();
                                    send(exchange, currentResponse);
                                    request.mark(clientRequest);

                                } else {
                                    final UnmatchedRequestReport report = new UnmatchedRequestReport(
                                        clientRequest,
                                        serverConfig.getExpectations().getRequests().stream().map(r -> (ErsatzRequest) r).collect(toList())
                                    );

                                    log.warn(report.toString());

                                    if (serverConfig.isMismatchToConsole()) {
                                        System.out.println(report);
                                    }

                                    exchange.setStatusCode(404).getResponseSender().send(NOT_FOUND_BODY);
                                }
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

            started = true;
        }
    }

    @Override public void stop() {
        if (started) {
            actualHttpPort = UNSPECIFIED_PORT;
            actualHttpsPort = UNSPECIFIED_PORT;

            if (server != null) {
                server.stop();
            }

            started = false;
        }
    }

    @Override public int getHttpPort() {
        return actualHttpPort;
    }

    @Override public int getHttpsPort() {
        return actualHttpsPort;
    }

    private void applyPorts() {
        actualHttpPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();

        if (serverConfig.isHttpsEnabled()) {
            actualHttpsPort = ((InetSocketAddress) server.getListenerInfo().get(1).getAddress()).getPort();
        }
    }

    private HttpHandler applyAuthentication(final HttpHandler handler) {
        HttpHandler result = handler;

        final AuthenticationConfig authConfig = serverConfig.getAuthenticationConfig();
        if (authConfig != null) {
            final SimpleIdentityManager identityManager = new SimpleIdentityManager(authConfig.getUsername(), authConfig.getPassword());
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
                    Thread.sleep(response.getDelay());
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
                exchange.getRequestHeaders().add(tryFromString("Transfer-encoding"), "chunked");
            }

            response.getCookies().forEach((k, v) -> {
                if (v instanceof Cookie) {
                    final Cookie ersatzCookie = (Cookie) v;

                    final CookieImpl chip = new CookieImpl(k, ersatzCookie.getValue());
                    chip.setPath(ersatzCookie.getPath());
                    chip.setDomain(ersatzCookie.getDomain());
                    chip.setMaxAge(ersatzCookie.getMaxAge());
                    chip.setSecure(ersatzCookie.getSecure());
                    chip.setVersion(ersatzCookie.getVersion());
                    chip.setHttpOnly(ersatzCookie.getHttpOnly());
                    chip.setComment(ersatzCookie.getComment());

                    exchange.getResponseCookies().put(k, chip);

                } else {
                    exchange.getResponseCookies().put(k, new CookieImpl(k, (String) v));
                }
            });
        }

        String responseContent = response != null ? response.getContent() : null;
        String responsePreview = responseContent != null ? responseContent : EMPTY;

        ChunkingConfig chunking = response != null ? response.getChunkingConfig() : null;
        if (responseContent != null && chunking != null) {
            log.debug("Chunked-Response({}; {}): {}", exchange.getRequestHeaders() != null && exchange.getRequestHeaders().size() > 0 ? exchange.getRequestHeaders() : NO_HEADERS, chunking, responsePreview);

            List<String> chunks = prepareChunks(responseContent, chunking.getChunks());
            exchange.getResponseSender().send(chunks.remove(0), new ResponseChunker(chunks, chunking.getDelay()));

        } else {
            log.debug("Response({}): {}", exchange.getResponseHeaders() != null && exchange.getResponseHeaders().size() > 0 ? exchange.getResponseHeaders() : NO_HEADERS, responsePreview);

            exchange.getResponseSender().send(responseContent);
        }
    }

    private SSLContext sslContext() throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("JKS");

        final URL url = serverConfig.getKeystoreLocation() != null ? serverConfig.getKeystoreLocation() : UndertowUnderlyingServer.class.getResource("/ersatz.keystore");

        try (final InputStream keyin = url.openStream()) {
            keyStore.load(keyin, serverConfig.getKeystorePass().toCharArray());
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, serverConfig.getKeystorePass().toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        return sslContext;
    }
}
