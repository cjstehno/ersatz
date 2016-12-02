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
package com.stehno.ersatz;

import com.stehno.ersatz.model.AbstractRequest;
import com.stehno.ersatz.model.ExpectationsImpl;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by cjstehno on 12/1/16.
 */
public class ErsatzServer {

    private final ExpectationsImpl expectations = new ExpectationsImpl();
    private Undertow server;
    private final int requestedPort;
    private int actualPort = -1;

    public ErsatzServer() {
        this(0);
    }

    public ErsatzServer(final int requestedPort) {
        this.requestedPort = requestedPort;
    }

    public int getPort() {
        return actualPort;
    }

    // FIXME: should be able to call this in global setup and then in local setup to apply additional
    public void requesting(final Consumer<ExpectationsImpl> expects) {
        expects.accept(expectations);
    }

    // FIXME: should be restartable
    public void start() {
        server = Undertow.builder()
            .addHttpListener(requestedPort, "localhost")
            .setHandler(new HttpHandler() {
                @Override
                public void handleRequest(final HttpServerExchange exchange) throws Exception {
                    final Optional<Request> optional = expectations.find(exchange);
                    if (optional.isPresent()) {
                        // TODO: maybe just get the active response here (see after other methods impl)
                        ((AbstractRequest) optional.get()).respond(exchange);
                    } else {
                        exchange.setStatusCode(404).getResponseSender().send("404 Not Found.");
                    }
                }
            }).build();

        server.start();

        actualPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();
    }

    public void stop() {
        actualPort = -1;

        if (server != null) {
            server.stop();
        }
    }

    // FIXME: find a way to be more explicit about failures (assertions)
    public boolean verify() {
        return expectations.verify();
    }
}
