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

import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.impl.*;
import io.github.cjstehno.ersatz.server.ClientRequest;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.ByteBuffer;

import static io.github.cjstehno.ersatz.server.UnderlyingServer.NOT_FOUND_BODY;
import static io.github.cjstehno.ersatz.server.undertow.ResponseChunker.prepareChunks;
import static io.undertow.util.HttpString.tryFromString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j @RequiredArgsConstructor
class ErsatzHttpHandler implements HttpHandler {

    private static final String TRANSFER_ENCODING = "Transfer-encoding";
    private static final String NO_HEADERS = "<no-headers>";
    private static final byte[] EMPTY_RESPONSE = new byte[0];
    private final RequirementsImpl requirements;
    private final ExpectationsImpl expectations;
    private final boolean reportToConsole;
    private final boolean logResponseContent;

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        val clientRequest = new UndertowClientRequest(exchange);

        log.debug("Request({}): {}", exchange.getProtocol(), clientRequest);

        // check the request against the global requirements
        if (!requirements.check(clientRequest)) {
            handleMismatch(exchange, clientRequest);
            return;
        }

        // check the request against the expectations
        expectations.findMatch(clientRequest).ifPresentOrElse(
            req -> {
                try {
                    val ereq = (ErsatzRequest) req;
                    send(exchange, ereq.getCurrentResponse());
                    ereq.mark(clientRequest);

                } catch (final Exception ex) {
                    log.error("Error-Response: Internal Server Error (500): {}", ex.getMessage(), ex);
                    exchange.setStatusCode(500);
                    sendFullResponse(exchange, EMPTY_RESPONSE);
                }
            },
            () -> handleMismatch(exchange, clientRequest)
        );
    }

    private void handleMismatch(final HttpServerExchange exchange, final ClientRequest clientRequest) {
        val report = new UnmatchedRequestReport(
            clientRequest,
            expectations.getRequests().stream().map(r -> (ErsatzRequest) r).toList(),
            requirements.getRequirements()
        );

        log.warn(report.render());

        if (reportToConsole) {
            System.out.println(report.render());
        }

        exchange.setStatusCode(404).getResponseSender().send(NOT_FOUND_BODY);
    }

    private void send(final HttpServerExchange exchange, final ErsatzResponse response) {
        if (response == null) {
            log.debug("Unconfigured-Response: No Content (204)");
            exchange.setStatusCode(204);
            sendFullResponse(exchange, EMPTY_RESPONSE);

        } else {
            applyResponseDelay(response);

            exchange.setStatusCode(response.getCode());

            applyResponseHeaders(exchange, response);

            applyResponseCookies(exchange, response);

            final var responseHeaders = exchange.getResponseHeaders() != null ? exchange.getResponseHeaders() : NO_HEADERS;
            final ChunkingConfigImpl chunking = response.getChunkingConfig();

            if (response.getContent().length > 0 && chunking != null) {
                log.debug("Chunked-Response({}; {}; {}; {}): {}", exchange.getProtocol(), exchange.getRequestURL(), responseHeaders, chunking, renderResponse(response));
                sendChunkedResponse(exchange, response.getContent(), chunking);

            } else {
                log.debug("Response({}; {}; {}): {}", exchange.getProtocol(), exchange.getRequestURL(), responseHeaders, renderResponse(response));
                sendFullResponse(exchange, response.getContent());
            }
        }
    }

    private String renderResponse(final ErsatzResponse response) {
        final var bytes = response.getContent();

        if (logResponseContent && isContentTypeRenderable(response.getContentType())) {
            return new String(response.getContent(), UTF_8);
        } else {
            return "<" + bytes.length + " bytes of " + response.getContentType() + " content>";
        }
    }

    private static boolean isContentTypeRenderable(final String contentType) {
        return contentType != null && (
            contentType.startsWith("text/") ||
                contentType.endsWith("/json") ||
                contentType.endsWith("/javascript") ||
                contentType.endsWith("/xml")
        );
    }

    private void sendChunkedResponse(final HttpServerExchange exchange, final byte[] responseContent, final ChunkingConfigImpl chunking) {
        final var chunks = prepareChunks(responseContent, chunking.getChunks());
        exchange.getResponseSender().send(ByteBuffer.wrap(chunks.remove(0)), new ResponseChunker(chunks, chunking.getDelay()));
    }

    private void sendFullResponse(final HttpServerExchange exchange, final byte[] responseContent) {
        exchange.getResponseSender().send(ByteBuffer.wrap(responseContent));
    }

    private void applyResponseDelay(final ErsatzResponse response) {
        if (response.getDelay() > 0) {
            try {
                log.trace("Delaying the response for {} ms...", response.getDelay());

                MILLISECONDS.sleep(response.getDelay());

            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void applyResponseHeaders(final HttpServerExchange exchange, final ErsatzResponse response) {
        response.getHeaders().forEach((k, v) -> {
            v.forEach(value -> exchange.getResponseHeaders().add(tryFromString(k), value));
        });

        if (response.getChunkingConfig() != null) {
            exchange.getResponseHeaders().add(tryFromString(TRANSFER_ENCODING), "chunked");
        }
    }

    private void applyResponseCookies(final HttpServerExchange exchange, final ErsatzResponse response) {
        response.getCookies().forEach((k, v) -> {
            if (v instanceof final Cookie ersatzCookie) {
                val cookie = new CookieImpl(k, ersatzCookie.getValue());
                cookie.setPath(ersatzCookie.getPath());
                cookie.setDomain(ersatzCookie.getDomain());
                cookie.setMaxAge(ersatzCookie.getMaxAge());
                cookie.setSecure(ersatzCookie.isSecure());
                cookie.setVersion(ersatzCookie.getVersion());
                cookie.setHttpOnly(ersatzCookie.isHttpOnly());
                cookie.setComment(ersatzCookie.getComment());
                exchange.setResponseCookie(cookie);
            } else {
                exchange.setResponseCookie(new CookieImpl(k, v.toString()));
            }
        });
    }
}
