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

import com.stehno.ersatz.auth.BasicAuthHandler;
import com.stehno.ersatz.auth.DigestAuthHandler;
import com.stehno.ersatz.auth.SimpleIdentityManager;
import com.stehno.ersatz.impl.*;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
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
import org.xnio.Options;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.stehno.ersatz.impl.ResponseChunker.prepareChunks;
import static groovy.lang.Closure.DELEGATE_FIRST;
import static io.undertow.UndertowOptions.*;
import static io.undertow.util.HttpString.tryFromString;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * The main entry point for configuring an Ersatz server, which allows configuring of the expectations and management of the server itself. This is
 * the class that should be instantiated in unit tests.
 * <p>
 * The server will be started on an ephemeral port so as not to collide with itself or other server applications running in the test environment. In
 * your tests, you can retrieve the server port or URL using the <code>getPort()</code> and <code>getServerUrl()</code> methods respectively.
 * <p>
 * Using the <code>ErsatzServer</code> follows the workflow:
 *
 * <ol>
 *     <li>Create the <code>ErsatzServer</code> instance.</li>
 *     <li>Configure the expectations.</li>
 *     <li>Start the server</li>
 *     <li>Run your client tests against the server.</li>
 *     <li>Verify the expectations.</li>
 *     <li>Stop the server.</li>
 * </ol>
 * <p>
 * See the <a href="http://stehno.com/ersatz/asciidoc/html5/" target="_blank">User Guide</a> for more detailed information.
 */
public class ErsatzServer implements ServerConfig, Closeable {

    /**
     * The response body returned when no matching expectation could be found.
     */
    public static final String NOT_FOUND_BODY = "404: Not Found";

    private static final Logger log = LoggerFactory.getLogger(ErsatzServer.class);
    private static final String LOCALHOST = "localhost";
    private static final int EPHEMERAL_PORT = 0;
    private static final int UNSPECIFIED_PORT = -1;
    private static final String NO_HEADERS = "<no-headers>";
    private static final String EMPTY = "<empty>";
    private final RequestDecoders globalDecoders = new RequestDecoders();
    private final ResponseEncoders globalEncoders = new ResponseEncoders();
    private final ExpectationsImpl expectations = new ExpectationsImpl(globalDecoders, globalEncoders);
    private Undertow server;
    private boolean httpsEnabled;
    private boolean autoStartEnabled = true;
    private boolean started;
    private boolean mismatchToConsole;
    private URL keystoreLocation;
    private String keystorePass = "ersatz";
    private int desiredHttpPort = EPHEMERAL_PORT;
    private int desiredHttpsPort = EPHEMERAL_PORT;
    private int actualHttpPort = UNSPECIFIED_PORT;
    private int actualHttpsPort = UNSPECIFIED_PORT;
    private AuthenticationConfig authenticationConfig;
    private Consumer<Undertow.Builder> timeoutConfig = b -> {
    };

    public ErsatzServer() {
        // nothing
    }

    /**
     * Creates a new Ersatz server instance with either the default configuration or a configuration provided by the Groovy DSL closure.
     *
     * @param closure the configuration closure (delegated to <code>ServerConfig</code>)
     */
    public ErsatzServer(@DelegatesTo(value = ServerConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        if (closure != null) {
            ConsumerWithDelegate.create(closure).accept(this);
        }
    }

    /**
     * Creates a new Ersatz server instance configured by the provided <code>Consumer</code>, which will have an instance of <code>ServerConfig</code>
     * passed into it for server configuration.
     *
     * @param consumer the configuration consumer
     */
    public ErsatzServer(final Consumer<ServerConfig> consumer) {
        consumer.accept(this);
    }

    /**
     * Used to control the enabled/disabled state of HTTPS on the server. By default HTTPS is disabled.
     *
     * @param enabled optional toggle value (true if not specified)
     * @return a reference to the server being configured
     */
    public ErsatzServer https(boolean enabled) {
        httpsEnabled = enabled;
        return this;
    }

    public ErsatzServer https() {
        return https(true);
    }

    /**
     * Used to enable/disable the auto-start feature, which will start the server after any call to either of the <code>expectations</code>
     * configuration methods. With this setting enabled, any other calls to the <code>start()</code> method are ignored. Further configuration is
     * allowed.
     * <p>
     * Auto-start is enabled by default.
     *
     * @param autoStart whether or not auto-start is enabled
     * @return a reference to the server being configured
     */
    public ServerConfig autoStart(boolean autoStart) {
        autoStartEnabled = autoStart;
        return this;
    }

    /**
     * Used to specify the server request timeout property value on the server. If not specified, <code>SECONDS</code> will be used as the units.
     * <p>
     * The IDLE_TIMEOUT, NO_REQUEST_TIMEOUT, REQUEST_PARSE_TIMEOUT, READ_TIMEOUT and WRITE_TIMEOUT are all configured to the same specified
     * value.
     *
     * @param value the timeout value
     * @param units the units the timeout is specified with (or <code>SECONDS</code>)
     * @return a reference to the server being configured
     */
    public ServerConfig timeout(final int value, final TimeUnit units) {
        timeoutConfig = builder -> {
            final var ms = (int) MILLISECONDS.convert(value, units);
            builder.setServerOption(IDLE_TIMEOUT, ms);
            builder.setServerOption(NO_REQUEST_TIMEOUT, ms);
            builder.setServerOption(REQUEST_PARSE_TIMEOUT, ms);
            builder.setSocketOption(Options.READ_TIMEOUT, ms);
            builder.setSocketOption(Options.WRITE_TIMEOUT, ms);
        };
        return this;
    }

    public ServerConfig timeout(final int value) {
        return timeout(value, SECONDS);
    }


    /**
     * Used to toggle the console output of mismatched request reports. By default they are only rendered in the logging. A value of <code>true</code>
     * will cause the report to be output on the console as well.
     *
     * @param toConsole whether or not the report should also be written to the console
     * @return a reference to the server being configured
     */
    @Override
    public ServerConfig reportToConsole(boolean toConsole) {
        mismatchToConsole = toConsole;
        return this;
    }

    public ServerConfig reportToConsole() {
        return reportToConsole(true);
    }

    /**
     * Allows configuration of an external HTTPS keystore with the given location and password. By default, if this is not specified an internally
     * provided keystore will be used for HTTPS certification. See the User Guide for details about configuring your own keystore.
     *
     * @param location the URL of the keystore file
     * @param password the keystore file password (defaults to "ersatz" if omitted)
     * @return a reference to the server being configured
     */
    public ServerConfig keystore(final URL location, final String password) {
        keystoreLocation = location;
        keystorePass = password;
        return this;
    }

    public ServerConfig keystore(final URL location) {
        return keystore(location, "ersatz");
    }

    /**
     * Used to retrieve the port where the HTTP server is running.
     *
     * @return the HTTP port
     */
    public int getHttpPort() {
        return actualHttpPort;
    }

    /**
     * Used to retrieve the port where the HTTPS server is running.
     *
     * @return the HTTPS port
     */
    public int getHttpsPort() {
        return actualHttpsPort;
    }

    /**
     * Used to retrieve the full URL of the HTTP server.
     *
     * @return the full URL of the HTTP server
     */
    public String getHttpUrl() {
        return "http://localhost:" + actualHttpPort;
    }

    /**
     * Used to retrieve the Web Socket URL.
     *
     * @return the web socket URL
     */
    public String getWsUrl() {
        return "ws://localhost:" + actualHttpPort;
    }

    /**
     * Used to retrieve the full URL of the HTTPS server.
     *
     * @return the full URL of the HTTP server
     */
    public String getHttpsUrl() {
        return "https://localhost:" + actualHttpsPort;
    }

    /**
     * A helper method which may be used to append the given path to the server HTTP url.
     *
     * @param path the path to be applied
     * @return the resulting URL
     */
    public String httpUrl(final String path) {
        return getHttpUrl() + path;
    }

    /**
     * A helper method which may be used to append the given path to the server HTTPS url.
     *
     * @param path the path to be applied
     * @return the resulting URL
     */
    public String httpsUrl(final String path) {
        return getHttpsUrl() + path;
    }

    /**
     * Used to configure HTTP expectations on the server; the provided <code>Consumer<Expectations></code> implementation will have an active
     * <code>Expectations</code> object passed into it for configuring server interaction expectations.
     * <p>
     * Calling this method when auto-start is enabled will start the server.
     *
     * @param expects the <code>Consumer<Expectations></code> instance to perform the configuration
     * @return a reference to this server
     */
    public ErsatzServer expectations(final Consumer<Expectations> expects) {
        expects.accept(expectations);

        if (autoStartEnabled) {
            start();
        }

        return this;
    }

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     * <p>
     * Calling this method when auto-start is enabled will start the server.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    public ErsatzServer expectations(@DelegatesTo(value = Expectations.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return expectations(ConsumerWithDelegate.create(closure));
    }

    /**
     * An alternate means of starting the expectation chain.
     * <p>
     * Calling this method when auto-start is enabled will <b>NOT</b> start the server. Use one of the other expectation configuration method if
     * auto-start functionality is desired.
     *
     * @return the reference to the Expectation configuration object
     */
    public Expectations expects() {
        return expectations;
    }

    /**
     * Configures the given request content decoder for the specified request content-type.
     *
     * @param contentType the request content-type
     * @param decoder     the request content decoder
     * @return the reference to the server configuration
     */
    @Override
    public ErsatzServer decoder(String contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        globalDecoders.register(contentType, decoder);
        return this;
    }

    /**
     * Configures the given request content decoder for the specified request content-type.
     *
     * @param contentType the request content-type
     * @param decoder     the request content decoder
     * @return the reference to the server configuration
     */
    @Override
    public ErsatzServer decoder(ContentType contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        globalDecoders.register(contentType, decoder);
        return this;
    }

    /**
     * Registers a response body encoder.
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param objectType the response object type to be encoded
     * @param encoder    the encoder function
     * @return a reference to this server configuration
     */
    @Override
    public ServerConfig encoder(String contentType, Class objectType, Function<Object, String> encoder) {
        globalEncoders.register(contentType, objectType, encoder);
        return this;
    }

    /**
     * Registers a response body encoder.
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param objectType the response object type to be encoded
     * @param encoder    the encoder function
     * @return a reference to this server configuration
     */
    @Override
    public ServerConfig encoder(ContentType contentType, Class objectType, Function<Object, String> encoder) {
        globalEncoders.register(contentType, objectType, encoder);
        return this;
    }

    /**
     * Registers authentication configuration as a Groovy Closure.
     *
     * @param closure the configuration closure
     * @return a reference to this server configuration
     */
    @Override
    public ServerConfig authentication(@DelegatesTo(value = AuthenticationConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return authentication(ConsumerWithDelegate.create(closure));
    }

    /**
     * Registers authentication configuration as a <code>Consumer<AuthenticationConfig></code>.
     *
     * @param config the configuration Consumer
     * @return a reference to this server configuration
     */
    @Override
    public ServerConfig authentication(final Consumer<AuthenticationConfig> config) {
        authenticationConfig = new AuthenticationConfig();
        config.accept(authenticationConfig);
        return this;
    }

    @Override
    public ServerConfig httpPort(int serverPort) {
        desiredHttpPort = serverPort;
        return this;
    }

    @Override
    public ServerConfig httpsPort(int serverPort) {
        desiredHttpsPort = serverPort;
        return this;
    }

    /**
     * Used to start the HTTP server for test interactions. This method should be called after configuration of expectations and before the test
     * interactions are executed against the server.
     */
    public void start() {
        if (!started) {
            final Undertow.Builder builder = Undertow.builder().addHttpListener(desiredHttpPort, LOCALHOST);
            timeoutConfig.accept(builder);

            if (httpsEnabled) {
                builder.addHttpsListener(desiredHttpsPort, LOCALHOST, sslContext());
            }

            BlockingHandler blockingHandler = new BlockingHandler(new EncodingHandler(
                applyAuthentication(
                    new HttpTraceHandler(
                        new HttpHandler() {
                            @Override
                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                                final ClientRequest clientRequest = new UndertowClientRequest(exchange);

                                log.debug("Request: {}", clientRequest);

                                final ErsatzRequest request = (ErsatzRequest) expectations.findMatch(clientRequest);
                                if (request != null) {
                                    Response currentResponse = request.getCurrentResponse();
                                    send(exchange, currentResponse);
                                    request.mark(clientRequest);

                                } else {
                                    final UnmatchedRequestReport report = new UnmatchedRequestReport(clientRequest, expectations.getRequests().stream().map(r -> (ErsatzRequest) r).collect(toList()));

                                    log.warn(report.toString());

                                    if (mismatchToConsole) {
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

            final WebSocketsHandlerBuilder wsBuilder = new WebSocketsHandlerBuilder(expectations, blockingHandler, mismatchToConsole);

            server = builder.setHandler(wsBuilder.build()).build();
            server.start();

            applyPorts();

            started = true;
        }
    }

    /**
     * Clears all configured expectations from the server. Does not affect global encoders or decoders.
     */
    public void clearExpectations() {
        expectations.clear();
    }

    /**
     * Used to stop the HTTP server. The server may be restarted after it has been stopped.
     */
    public void stop() {
        if (started) {
            actualHttpPort = UNSPECIFIED_PORT;
            actualHttpsPort = UNSPECIFIED_PORT;

            if (server != null) {
                server.stop();
            }

            started = false;
        }
    }

    /**
     * An alias to the <code>stop()</code> method.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Used to verify that all of the expected request interactions were called the appropriate number of times. This method should be called after
     * all test interactions have been performed. This is an optional step since generally you will also be receiving the expected response back
     * from the server; however, this verification step can come in handy when simply needing to know that a request is actually called or not.
     * <p>
     * If there are web socket expectations configured, this method will be blocking against the expected operations. Expectations involving web
     * sockets should consider using the timeout parameters - the default is 1s.
     *
     * @param timeout the timeout value (defaults to 1)
     * @param unit    the timeout unit (defaults to SECONDS)
     * @return <code> true</code> if all call criteria were met during test execution.
     */
    public boolean verify(final long timeout, final TimeUnit unit) {
        return expectations.verify(timeout, unit);
    }

    public boolean verify(final long timeout) {
        return verify(timeout, SECONDS);
    }

    public boolean verify() {
        return verify(1, SECONDS);
    }

    private HttpHandler applyAuthentication(final HttpHandler handler) {
        HttpHandler result = handler;

        if (authenticationConfig != null) {
            SimpleIdentityManager identityManager = new SimpleIdentityManager(authenticationConfig.getUsername(), authenticationConfig.getPassword());
            switch (authenticationConfig.getType()) {
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

        if (httpsEnabled) {
            actualHttpsPort = ((InetSocketAddress) server.getListenerInfo().get(1).getAddress()).getPort();
        }
    }

    private SSLContext sslContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            final var location = keystoreLocation != null ? keystoreLocation : ErsatzServer.class.getResource("/ersatz.keystore");

            try (final InputStream instr = location.openStream()) {
                keyStore.load(instr, keystorePass.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePass.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext;

        } catch (Exception ex) {
            // FIXME: better?
            throw new IllegalStateException(ex);
        }
    }
}
