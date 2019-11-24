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
package com.stehno.ersatz

import com.stehno.ersatz.auth.BasicAuthHandler
import com.stehno.ersatz.auth.DigestAuthHandler
import com.stehno.ersatz.auth.SimpleIdentityManager
import com.stehno.ersatz.impl.ErsatzRequest
import com.stehno.ersatz.impl.ExpectationsImpl
import com.stehno.ersatz.impl.ResponseChunker
import com.stehno.ersatz.impl.UndertowClientRequest
import com.stehno.ersatz.impl.UnmatchedRequestReport
import com.stehno.ersatz.impl.WebSocketsHandlerBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.CookieImpl
import io.undertow.server.handlers.HttpTraceHandler
import io.undertow.server.handlers.encoding.ContentEncodingRepository
import io.undertow.server.handlers.encoding.DeflateEncodingProvider
import io.undertow.server.handlers.encoding.EncodingHandler
import io.undertow.server.handlers.encoding.GzipEncodingProvider
import org.xnio.Options
import space.jasan.support.groovy.closure.ConsumerWithDelegate

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

import static com.stehno.ersatz.impl.ResponseChunker.prepareChunks
import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.transform.TypeCheckingMode.SKIP
import static io.undertow.UndertowOptions.IDLE_TIMEOUT
import static io.undertow.UndertowOptions.NO_REQUEST_TIMEOUT
import static io.undertow.UndertowOptions.REQUEST_PARSE_TIMEOUT
import static io.undertow.util.HttpString.tryFromString
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

/**
 * The main entry point for configuring an Ersatz server, which allows configuring of the expectations and management of the server itself. This is
 * the class that should be instantiated in unit tests.
 *
 * The server will be started on an ephemeral port so as not to collide with itself or other server applications running in the test environment. In
 * your tests, you can retrieve the server port or URL using the <code>getPort()</code> and <code>getServerUrl()</code> methods respectively.
 *
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
 *
 * See the <a href="http://stehno.com/ersatz/asciidoc/html5/" target="_blank">User Guide</a> for more detailed information.
 */
@CompileStatic
@Slf4j
class ErsatzServer implements ServerConfig, Closeable {

    /**
     * The response body returned when no matching expectation could be found.
     */
    static final String NOT_FOUND_BODY = '404: Not Found'

    private static final String LOCALHOST = 'localhost'
    private static final int EPHEMERAL_PORT = 0
    private static final int UNSPECIFIED_PORT = -1
    private static final String NO_HEADERS = '<no-headers>'
    private static final String EMPTY = '<empty>'
    private final RequestDecoders globalDecoders = new RequestDecoders()
    private final ResponseEncoders globalEncoders = new ResponseEncoders()
    private final ExpectationsImpl expectations = new ExpectationsImpl(globalDecoders, globalEncoders)
    private Undertow server
    private boolean httpsEnabled
    private boolean autoStartEnabled = true
    private boolean started
    private boolean mismatchToConsole
    private URL keystoreLocation
    private String keystorePass = 'ersatz'
    private int desiredHttpPort = EPHEMERAL_PORT
    private int desiredHttpsPort = EPHEMERAL_PORT
    private int actualHttpPort = UNSPECIFIED_PORT
    private int actualHttpsPort = UNSPECIFIED_PORT
    private AuthenticationConfig authenticationConfig
    private Closure<Void> timeoutConfig = { b -> }

    /**
     * Creates a new Ersatz server instance with either the default configuration or a configuration provided by the Groovy DSL closure.
     *
     * @param closure the configuration closure (delegated to <code>ServerConfig</code>)
     */
    ErsatzServer(@DelegatesTo(value = ServerConfig, strategy = DELEGATE_FIRST) final Closure closure = null) {
        if (closure) {
            ConsumerWithDelegate.create(closure).accept(this)
        }
    }

    /**
     * Creates a new Ersatz server instance configured by the provided <code>Consumer</code>, which will have an instance of <code>ServerConfig</code>
     * passed into it for server configuration.
     *
     * @param consumer the configuration consumer
     */
    ErsatzServer(final Consumer<ServerConfig> consumer) {
        consumer.accept(this)
    }

    /**
     * Used to control the enabled/disabled state of HTTPS on the server. By default HTTPS is disabled.
     *
     * @param enabled optional toggle value (true if not specified)
     * @return a reference to the server being configured
     */
    ErsatzServer https(boolean enabled = true) {
        httpsEnabled = enabled
        this
    }

    /**
     * Used to enable/disable the auto-start feature, which will start the server after any call to either of the <code>expectations</code>
     * configuration methods. With this setting enabled, any other calls to the <code>start()</code> method are ignored. Further configuration is
     * allowed.
     *
     * Auto-start is enabled by default.
     *
     * @param autoStart whether or not auto-start is enabled
     * @return a reference to the server being configured
     */
    ServerConfig autoStart(boolean autoStart) {
        autoStartEnabled = autoStart
        this
    }

    /**
     * Used to specify the server request timeout property value on the server. If not specified, <code>SECONDS</code> will be used as the units.
     *
     * The IDLE_TIMEOUT, NO_REQUEST_TIMEOUT, REQUEST_PARSE_TIMEOUT, READ_TIMEOUT and WRITE_TIMEOUT are all configured to the same specified
     * value.
     *
     * @param value the timeout value
     * @param units the units the timeout is specified with (or <code>SECONDS</code>)
     * @return a reference to the server being configured
     */
    ServerConfig timeout(final int value, final TimeUnit units = SECONDS) {
        timeoutConfig = { Undertow.Builder builder ->
            Integer ms = MILLISECONDS.convert(value, units) as Integer
            builder.setServerOption(IDLE_TIMEOUT, ms)
            builder.setServerOption(NO_REQUEST_TIMEOUT, ms)
            builder.setServerOption(REQUEST_PARSE_TIMEOUT, ms)
            builder.setSocketOption(Options.READ_TIMEOUT, ms)
            builder.setSocketOption(Options.WRITE_TIMEOUT, ms)
            null
        }
        this
    }

    /**
     * Used to toggle the console output of mismatched request reports. By default they are only rendered in the logging. A value of <code>true</code>
     * will cause the report to be output on the console as well.
     *
     * @param toConsole whether or not the report should also be written to the console
     * @return a reference to the server being configured
     */
    @Override
    ServerConfig reportToConsole(boolean toConsole = true) {
        mismatchToConsole = toConsole
        this
    }

    /**
     * Allows configuration of an external HTTPS keystore with the given location and password. By default, if this is not specified an internally
     * provided keystore will be used for HTTPS certification. See the User Guide for details about configuring your own keystore.
     *
     * @param location the URL of the keystore file
     * @param password the keystore file password (defaults to "ersatz" if omitted)
     * @return a reference to the server being configured
     */
    ServerConfig keystore(final URL location, final String password = 'ersatz') {
        keystoreLocation = location
        keystorePass = password
        this
    }

    /**
     * Used to retrieve the port where the HTTP server is running.
     *
     * @return the HTTP port
     */
    int getHttpPort() {
        actualHttpPort
    }

    /**
     * Used to retrieve the port where the HTTPS server is running.
     *
     * @return the HTTPS port
     */
    int getHttpsPort() {
        actualHttpsPort
    }

    /**
     * Used to retrieve the full URL of the HTTP server.
     *
     * @return the full URL of the HTTP server
     */
    String getHttpUrl() {
        "http://localhost:$actualHttpPort"
    }

    /**
     * Used to retrieve the Web Socket URL.
     *
     * @return the web socket URL
     */
    String getWsUrl() {
        "ws://localhost:$actualHttpPort"
    }

    /**
     * Used to retrieve the full URL of the HTTPS server.
     *
     * @return the full URL of the HTTP server
     */
    String getHttpsUrl() {
        "https://localhost:$actualHttpsPort"
    }

    /**
     * A helper method which may be used to append the given path to the server HTTP url.
     *
     * @param path the path to be applied
     * @return the resulting URL
     */
    String httpUrl(final String path) {
        "$httpUrl$path"
    }

    /**
     * A helper method which may be used to append the given path to the server HTTPS url.
     *
     * @param path the path to be applied
     * @return the resulting URL
     */
    String httpsUrl(final String path) {
        "$httpsUrl$path"
    }

    /**
     * Used to configure HTTP expectations on the server; the provided <code>Consumer<Expectations></code> implementation will have an active
     * <code>Expectations</code> object passed into it for configuring server interaction expectations.
     *
     * Calling this method when auto-start is enabled will start the server.
     *
     * @param expects the <code>Consumer<Expectations></code> instance to perform the configuration
     * @return a reference to this server
     */
    ErsatzServer expectations(final Consumer<Expectations> expects) {
        expects.accept(expectations)

        if (autoStartEnabled) {
            start()
        }

        this
    }

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     *
     * Calling this method when auto-start is enabled will start the server.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    ErsatzServer expectations(@DelegatesTo(value = Expectations, strategy = DELEGATE_FIRST) final Closure closure) {
        expectations(ConsumerWithDelegate.create(closure))
    }

    /**
     * An alternate means of starting the expectation chain.
     *
     * Calling this method when auto-start is enabled will <b>NOT</b> start the server. Use one of the other expectation configuration method if
     * auto-start functionality is desired.
     *
     * @return the reference to the Expectation configuration object
     */
    Expectations expects() {
        expectations
    }

    /**
     * Configures the given request content decoder for the specified request content-type.
     *
     * @param contentType the request content-type
     * @param decoder the request content decoder
     * @return the reference to the server configuration
     */
    @Override
    ErsatzServer decoder(String contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        globalDecoders.register contentType, decoder
        this
    }

    /**
     * Configures the given request content decoder for the specified request content-type.
     *
     * @param contentType the request content-type
     * @param decoder the request content decoder
     * @return the reference to the server configuration
     */
    @Override
    ErsatzServer decoder(ContentType contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        globalDecoders.register contentType, decoder
        this
    }

    /**
     * Registers a response body encoder.
     *
     * param contentType the response content-type to be encoded
     * @param objectType the response object type to be encoded
     * @param encoder the encoder function
     * @return a reference to this server configuration
     */
    @Override
    ServerConfig encoder(String contentType, Class objectType, Function<Object, String> encoder) {
        globalEncoders.register contentType, objectType, encoder
        this
    }

    /**
     * Registers a response body encoder.
     *
     * param contentType the response content-type to be encoded
     * @param objectType the response object type to be encoded
     * @param encoder the encoder function
     * @return a reference to this server configuration
     */
    @Override
    ServerConfig encoder(ContentType contentType, Class objectType, Function<Object, String> encoder) {
        globalEncoders.register contentType, objectType, encoder
        this
    }

    /**
     * Registers authentication configuration as a Groovy Closure.
     *
     * @param closure the configuration closure
     * @return a reference to this server configuration
     */
    @Override
    ServerConfig authentication(@DelegatesTo(value = AuthenticationConfig, strategy = DELEGATE_FIRST) final Closure closure) {
        authentication(ConsumerWithDelegate.create(closure))
    }

    /**
     * Registers authentication configuration as a <code>Consumer<AuthenticationConfig></code>.
     *
     * @param config the configuration Consumer
     * @return a reference to this server configuration
     */
    @Override
    ServerConfig authentication(final Consumer<AuthenticationConfig> config) {
        authenticationConfig = new AuthenticationConfig()
        config.accept(authenticationConfig)
        return this
    }

    @Override
    ServerConfig httpPort(int serverPort) {
        desiredHttpPort = serverPort
        return this
    }

    @Override
    ServerConfig httpsPort(int serverPort) {
        desiredHttpsPort = serverPort
        return this
    }

    /**
     * Used to start the HTTP server for test interactions. This method should be called after configuration of expectations and before the test
     * interactions are executed against the server.
     */
    void start() {
        if (!started) {
            Undertow.Builder builder = Undertow.builder().addHttpListener(desiredHttpPort, LOCALHOST)
            timeoutConfig.call(builder)

            if (httpsEnabled) {
                builder.addHttpsListener(desiredHttpsPort, LOCALHOST, sslContext())
            }

            BlockingHandler blockingHandler = new BlockingHandler(new EncodingHandler(
                    applyAuthentication(
                            new HttpTraceHandler(
                                    new HttpHandler() {
                                        @Override
                                        void handleRequest(final HttpServerExchange exchange) throws Exception {
                                            ClientRequest clientRequest = new UndertowClientRequest(exchange)

                                            log.debug 'Request: {}', clientRequest

                                            ErsatzRequest request = expectations.findMatch(clientRequest) as ErsatzRequest
                                            if (request) {
                                                Response currentResponse = request.currentResponse
                                                send(exchange, currentResponse)
                                                request.mark(clientRequest)

                                            } else {
                                                UnmatchedRequestReport report = new UnmatchedRequestReport(
                                                        clientRequest,
                                                        expectations.requests as List<ErsatzRequest>
                                                )

                                                log.warn report.toString()

                                                if (mismatchToConsole) {
                                                    println report
                                                }

                                                exchange.setStatusCode(404).responseSender.send(NOT_FOUND_BODY)
                                            }
                                        }
                                    }
                            )
                    ),
                    new ContentEncodingRepository()
                            .addEncodingHandler('gzip', new GzipEncodingProvider(), 50)
                            .addEncodingHandler('deflate', new DeflateEncodingProvider(), 50)
            ))

            WebSocketsHandlerBuilder wsBuilder = new WebSocketsHandlerBuilder(expectations, blockingHandler, mismatchToConsole)

            server = builder.setHandler(wsBuilder.build()).build()
            server.start()

            applyPorts()

            started = true
        }
    }

    /**
     * Clears all configured expectations from the server. Does not affect global encoders or decoders.
     */
    void clearExpectations() {
        expectations.clear()
    }

    /**
     * Used to stop the HTTP server. The server may be restarted after it has been stopped.
     */
    void stop() {
        if (started) {
            actualHttpPort = UNSPECIFIED_PORT
            actualHttpsPort = UNSPECIFIED_PORT

            server?.stop()

            started = false
        }
    }

    /**
     * An alias to the <code>stop()</code> method.
     */
    @Override
    void close() {
        stop()
    }

    /**
     * Used to verify that all of the expected request interactions were called the appropriate number of times. This method should be called after
     * all test interactions have been performed. This is an optional step since generally you will also be receiving the expected response back
     * from the server; however, this verification step can come in handy when simply needing to know that a request is actually called or not.
     *
     * If there are web socket expectations configured, this method will be blocking against the expected operations. Expectations involving web
     * sockets should consider using the timeout parameters - the default is 1s.
     *
     * @param timeout the timeout value (defaults to 1)
     * @param unit the timeout unit (defaults to SECONDS)
     * @return <code> true</code> if all call criteria were met during test execution.
     */
    boolean verify(final long timeout = 1, final TimeUnit unit = SECONDS) {
        expectations.verify(timeout, unit)
    }

    private HttpHandler applyAuthentication(final HttpHandler handler) {
        HttpHandler result = handler

        if (authenticationConfig) {
            SimpleIdentityManager identityManager = new SimpleIdentityManager(authenticationConfig.username, authenticationConfig.password)
            switch (authenticationConfig.type) {
                case Authentication.BASIC:
                    result = new BasicAuthHandler(identityManager).apply(result)
                    break
                case Authentication.DIGEST:
                    result = new DigestAuthHandler(identityManager).apply(result)
                    break
                default:
                    throw new IllegalArgumentException('Invalid authentication configuration.')
            }
        }

        result
    }

    private static void send(final HttpServerExchange exchange, final Response response) {
        if (response) {
            if (response.delay) {
                sleep response.delay
            }

            exchange.statusCode = response.code

            response.headers.each { String k, List<String> v ->
                v.each { String value ->
                    exchange.responseHeaders.add(tryFromString(k), value)
                }
            }

            if (response.chunkingConfig) {
                exchange.responseHeaders.add(tryFromString('Transfer-encoding'), 'chunked')
            }

            response.cookies.each { k, v ->
                if (v instanceof Cookie) {
                    Cookie ersatzCookie = v as Cookie
                    exchange.responseCookies.put k, new CookieImpl(k, ersatzCookie.value).with {
                        path = ersatzCookie.path
                        domain = ersatzCookie.domain
                        maxAge = ersatzCookie.maxAge
                        secure = ersatzCookie.secure
                        version = ersatzCookie.version
                        httpOnly = ersatzCookie.httpOnly
                        setComment(ersatzCookie.comment)
                    } as CookieImpl

                } else {
                    exchange.responseCookies.put(k, new CookieImpl(k, v as String))
                }
            }
        }

        String responseContent = response?.content
        String responsePreview = responseContent?.take(1000) ?: EMPTY

        ChunkingConfig chunking = response?.chunkingConfig
        if (responseContent && chunking) {
            log.debug 'Chunked-Response({}; {}): {}', exchange.responseHeaders ?: NO_HEADERS, chunking, responsePreview

            List<String> chunks = prepareChunks(responseContent, chunking.chunks)
            exchange.responseSender.send(chunks.remove(0), new ResponseChunker(chunks, chunking.delay))

        } else {
            log.debug 'Response({}): {}', exchange.responseHeaders ?: NO_HEADERS, responsePreview

            exchange.responseSender.send(responseContent)
        }
    }

    private void applyPorts() {
        actualHttpPort = (server.listenerInfo[0].address as InetSocketAddress).port

        if (httpsEnabled) {
            actualHttpsPort = (server.listenerInfo[1].address as InetSocketAddress).port
        }
    }

    @CompileStatic(SKIP)
    private SSLContext sslContext() {
        KeyStore keyStore = KeyStore.getInstance('JKS')

        (keystoreLocation ?: ErsatzServer.getResource('/ersatz.keystore')).withInputStream { instr ->
            keyStore.load(instr, keystorePass.toCharArray())
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.defaultAlgorithm)
        keyManagerFactory.init(keyStore, keystorePass.toCharArray())

        SSLContext sslContext = SSLContext.getInstance('TLS')
        sslContext.init(keyManagerFactory.keyManagers, null, null)

        sslContext
    }
}
