/**
 * Copyright (C) 2023 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.server.undertow;

import io.github.cjstehno.ersatz.cfg.Response;
import io.github.cjstehno.ersatz.impl.ErsatzForwardResponse;
import io.github.cjstehno.ersatz.server.ClientRequest;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.Set;

/**
 * An Ersatz Undertow handler used to handle request forwarding to gather response data from an external server URI.
 * <p>
 * This implementation utilizes the built-in Java <code>HttpClient</code> framework.
 */
@RequiredArgsConstructor @Slf4j
public class ErsatzForwardHandler implements ErsatzHandler {

    private static final Set<String> RESTRICTED_HEADER_NAMES = Set.of(
        "accept-charset", "accept-encoding", "access-control-request-headers", "access-control-request-method",
        "connection", "content-length", "cookie", "date", "dnt", "expect", "host", "keep-alive", "origin",
        "permissions-policy", "referer", "te", "trailer", "transfer-encoding", "upgrade", "via"
    );
    private final ErsatzHandler next;

    /**
     * Handles the request. It will determine whether the request should be forwarded to an external resource to retrieve
     * its response, otherwise it will continue the standard Ersatz processing.
     *
     * @param exchange       the server exchange object for the current request/response cycle
     * @param clientRequest  the incoming client request
     * @param ersatzResponse the configured outgoing response
     * @throws Exception if there is a problem handling the request/response
     */
    public void handleRequest(final HttpServerExchange exchange, final ClientRequest clientRequest, final Response ersatzResponse) throws Exception {
        if (ersatzResponse instanceof ErsatzForwardResponse) {
            val fullTargetUri = ((ErsatzForwardResponse) ersatzResponse).getProxyTargetUri() + exchange.getRequestPath();
            log.info("Request forwarding to: {}", fullTargetUri);

            val requestBuilder = HttpRequest.newBuilder(new URI(fullTargetUri))
                .method(exchange.getRequestMethod().toString(), HttpRequest.BodyPublishers.ofByteArray(clientRequest.getBody()));

            // copy request headers
            exchange.getRequestHeaders().forEach(header -> {
                if (!isRestrictedHeader(header.getHeaderName())) {
                    requestBuilder.header(header.getHeaderName().toString(), String.join(";", header));
                }
            });

            val response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

            // copy response headers
            response.headers().map().forEach((name, values) -> {
                exchange.getResponseHeaders().putAll(new HttpString(name), values);
            });

            // copy response content
            exchange.getResponseSender().send(ByteBuffer.wrap(response.body()));

        } else {
            next.handleRequest(exchange, clientRequest, ersatzResponse);
        }
    }

    // certain headers cannot be set - if this becomes an issue, the okhttp client seems to allow it (switch)
    private static boolean isRestrictedHeader(final HttpString name) {
        val headerName = name.toString().toLowerCase();
        return headerName.startsWith("proxy-") || headerName.startsWith("sec-") || RESTRICTED_HEADER_NAMES.contains(headerName);
    }
}
