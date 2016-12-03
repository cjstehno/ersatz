/**
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

import com.stehno.ersatz.model.AbstractRequest
import com.stehno.ersatz.model.ExpectationsImpl
import groovy.transform.CompileStatic
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

import java.util.function.Consumer

/**
 * Main entry point for the Ersatz server.
 */
@CompileStatic
class ErsatzServer {

    // FIXME: need a means of adding "plugins" for supporting things like BASIC, DIGEST, OAUTH

    int requestedPort

    private final ExpectationsImpl expectations = new ExpectationsImpl()
    private Undertow server
    private int actualPort = -1

    ErsatzServer() {
        this(0)
    }

    ErsatzServer(final int requestedPort) {
        this.requestedPort = requestedPort
    }

    int getPort() {
        actualPort
    }

    String getServerUrl() {
        "http://localhost:$actualPort"
    }

    // FIXME: should be able to call this in global setup and then in local setup to apply additional
    ErsatzServer expectations(final Consumer<Expectations> expects) {
        expects.accept(expectations)
        this
    }

    ErsatzServer expectations(@DelegatesTo(Expectations) final Closure closure) {
        closure.delegate = expectations
        closure.call()

        this
    }

    // FIXME: record and allow access to all requests (optional) - just logging?
    // FIXME: 404 should provide info about the missed request  (optional)

    // FIXME: should be restartable
    void start() {
        server = Undertow.builder()
            .addHttpListener(requestedPort, 'localhost')
            .setHandler(new HttpHandler() {
            @Override
            void handleRequest(final HttpServerExchange exchange) throws Exception {
                Request request = expectations.find(exchange)
                if (request) {
                    // TODO: maybe just get the active response here (see after other methods impl)
                    ((AbstractRequest) request).respond(exchange)
                } else {
                    exchange.setStatusCode(404).responseSender.send('404 Not Found.')
                }
            }
        }).build()

        server.start()

        actualPort = (server.listenerInfo[0].address as InetSocketAddress).port
    }

    void stop() {
        actualPort = -1

        server?.stop()
    }

    // FIXME: find a way to be more explicit about failures (assertions)
    boolean verify() {
        expectations.verify()
    }
}
