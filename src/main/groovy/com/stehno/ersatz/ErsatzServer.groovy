/*
 * Copyright (C) 2016 Christopher J. Stehno
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

import com.stehno.ersatz.impl.ErsatzRequest
import com.stehno.ersatz.impl.ExpectationsImpl
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.CookieImpl
import io.undertow.util.HttpString

import java.util.function.Consumer

/**
 * The main entry point for an Ersatz server, used to configure the expectations and manage the server itself. This is the class that should be
 * instantiated in unit tests.
 *
 * The server will be started on an ephemeral port so as not to collide with itself or other server applications running in the test environment. In
 * your tests, you can retrieve the server port or URL using the <code>getPort()</code> and <code>getServerUrl()</code> methods respectively.
 *
 * TODO: document configuration steps
 */
@CompileStatic @Slf4j
class ErsatzServer {

    static final String NOT_FOUND_BODY = '404: Not Found'

    List<ServerFeature> features = []

    private final ExpectationsImpl expectations = new ExpectationsImpl()
    private Undertow server
    private int actualPort = -1

    /**
     * Used to enable support for a feature extension.
     *
     * @param feature the <code>ServerFeature</code> to be added
     */
    void addFeature(ServerFeature feature) {
        features << feature
    }

    /**
     * Used to retrieve the port where the HTTP server is running.
     *
     * @return the HTTP server port
     */
    int getPort() {
        actualPort
    }

    /**
     * Used to retrieve the full URL of the HTTP server.
     *
     * @return the full URL of the HTTP server
     */
    String getServerUrl() {
        "http://localhost:$actualPort"
    }

    /**
     * Used to configure HTTP expectations on the server; the provided <code>Consumer<Expectations></code> implementation will have an active
     * <code>Expectations</code> object passed into it for configuring server interaction expectations.
     *
     * @param expects the <code>Consumer<Expectations></code> instance to perform the configuration
     * @return a reference to this server
     */
    @SuppressWarnings('ConfusingMethodName')
    ErsatzServer expectations(final Consumer<Expectations> expects) {
        expects.accept(expectations)
        this
    }

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    @SuppressWarnings('ConfusingMethodName')
    ErsatzServer expectations(@DelegatesTo(Expectations) final Closure closure) {
        closure.delegate = expectations
        closure.call()
        this
    }

    /**
     * Used to start the HTTP server for test interactions. This method should be called after configuration of expectations and before the test
     * interactions are executed against the server.
     */
    void start() {
        server = Undertow.builder().addHttpListener(0, 'localhost').setHandler(applyFeatures(new HttpHandler() {
            @Override
            void handleRequest(final HttpServerExchange exchange) throws Exception {
                log.debug 'Request: {}', dump(exchange)

                ErsatzRequest request = expectations.findMatch(exchange) as ErsatzRequest
                if (request) {
                    send(exchange, request.currentResponse)
                    request.mark()

                } else {
                    log.warn 'Unmatched-Request: {}', dump(exchange)

                    exchange.setStatusCode(404).responseSender.send(NOT_FOUND_BODY)
                }
            }
        })).build()

        server.start()

        actualPort = (server.listenerInfo[0].address as InetSocketAddress).port
    }

    private String dump(final HttpServerExchange exchange){
        "${exchange.requestMethod} ${exchange.requestPath} (query=${exchange.queryParameters}, headers=${exchange.requestHeaders}, cookies=${exchange.requestCookies})"
    }

    /**
     * Used to stop the HTTP server.
     */
    void stop() {
        actualPort = -1

        server?.stop()
    }

    /**
     * Used to verify all of the HTTP server interaction for their expected call criteria (if any). This method should be called after any test
     * interactions have been performed.
     *
     * @return <code>true</code> if all call criteria were met during test execution.
     */
    boolean verify() {
        expectations.verify()
    }

    private HttpHandler applyFeatures(final HttpHandler handler) {
        HttpHandler result = handler

        features?.each { feat ->
            result = feat.apply(result)
        }

        result
    }

    private static void send(final HttpServerExchange exchange, final Response response) {
        exchange.statusCode = response.code

        response.headers.each { k, v ->
            exchange.responseHeaders.put(new HttpString(k), v)
        }

        response.cookies.each { k, v ->
            exchange.responseCookies.put(k, new CookieImpl(k, v))
        }

        exchange.responseSender.send(response.body?.toString() ?: '')
    }
}
