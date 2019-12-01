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

import com.stehno.ersatz.encdec.Cookie;
import com.stehno.ersatz.impl.*;
import com.stehno.ersatz.server.ClientRequest;
import com.stehno.ersatz.server.UnderlyingServer;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.stehno.ersatz.server.undertow.ResponseChunker.prepareChunks;
import static io.undertow.util.HttpString.tryFromString;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

class ErsatzHttpHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(ErsatzHttpHandler.class);
    private static final String NO_HEADERS = "<no-headers>";
    private static final String EMPTY = "<empty>";
    private final ExpectationsImpl expectations;
    private final boolean reportToConsole;

    ErsatzHttpHandler(final ExpectationsImpl expectations, final boolean reportToConsole) {
        this.expectations = expectations;
        this.reportToConsole = reportToConsole;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        final ClientRequest clientRequest = new UndertowClientRequest(exchange);

        log.debug("Request({}): {}", exchange.getProtocol(), clientRequest);

        expectations.findMatch(clientRequest)
            .ifPresentOrElse(
                req -> {
                    final var ereq = (ErsatzRequest) req;
                    send(exchange, ereq.getCurrentResponse());
                    ereq.mark(clientRequest);
                },
                () -> {
                    final var report = new UnmatchedRequestReport(clientRequest, expectations.getRequests().stream().map(r -> (ErsatzRequest) r).collect(toList()));

                    log.warn(report.render());

                    if (reportToConsole) {
                        System.out.println(report);
                    }

                    exchange.setStatusCode(404).getResponseSender().send(UnderlyingServer.NOT_FOUND_BODY);
                }
            );
    }

    private void send(final HttpServerExchange exchange, final ErsatzResponse response) {
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
        final ChunkingConfigImpl chunking = response != null ? response.getChunkingConfig() : null;
        final var responseHeaders = exchange.getResponseHeaders() != null ? exchange.getResponseHeaders() : NO_HEADERS;

        if (responseContent != null && chunking != null) {
            log.debug("Chunked-Response({}; {}): {}", responseHeaders, chunking, responsePreview);

            final List<String> chunks = prepareChunks(responseContent, chunking.getChunks());
            exchange.getResponseSender().send(chunks.remove(0), new ResponseChunker(chunks, chunking.getDelay()));

        } else {
            log.debug("Response({}; {}): {}", exchange.getProtocol(), responseHeaders, responsePreview);

            exchange.getResponseSender().send(responseContent);
        }
    }
}
