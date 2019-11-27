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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.ClientRequest;
import com.stehno.ersatz.HttpMethod;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER;
import static java.lang.String.format;

/**
 * The primary <code>ClientRequest</code> implementation used to wrap and expose the important parts of the underlying Undertow request context.
 */
public class UndertowClientRequest implements ClientRequest {

    private final HttpServerExchange exchange;
    private AtomicReference<byte[]> content;

    public UndertowClientRequest(final HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    /**
     * Used to retrieve the request protocol, generally HTTP or HTTPS.
     *
     * @return the request protocol
     */
    public String getProtocol() {
        return exchange.getRequestScheme();
    }

    /**
     * Retrieves the HTTP method for the request.
     *
     * @return the HTTP method for the request
     */
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(exchange.getRequestMethod().toString());
    }

    /**
     * Retrieves the request path.
     *
     * @return the request path
     */
    public String getPath() {
        return exchange.getRequestPath();
    }

    /**
     * Retrieves the URL query string parameters for the request.
     *
     * @return the query string parameters
     */
    public Map<String, Deque<String>> getQueryParams() {
        return exchange.getQueryParameters();
    }

    /**
     * Retrieves the request headers.
     *
     * @return the request headers
     */
    public HeaderMap getHeaders() {
        return exchange.getRequestHeaders();
    }

    /**
     * Retrieves the cookies associated with the request.
     *
     * @return the request cookies
     */
    public Map<String, Cookie> getCookies() {
        return exchange.getRequestCookies();
    }

    /**
     * Retrieves the body content (if any) as a byte array (null for an empty request).
     *
     * @return the optional body content as a byte array.
     */
    public byte[] getBody() {
        if (content == null) {
            content = new AtomicReference<>();

            exchange.getRequestReceiver().receiveFullBytes((exch, message) -> content.set(message));
        }

        return content.get();
    }

    @Override
    public String toString() {
        String contentString = "<empty>";

        final var body = getBody();
        if (body != null && getContentType() != null && getContentType().startsWith("text/")) {
            contentString = new String(body);
        } else if (body != null) {
            contentString = format("<%d of %s content>", getContentLength(), getContentType());
        }

        return format("{ %s %s(query=%s, headers=%s, cookies=%s): %s }", getMethod(), getPath(), getQueryParams(), getHeaders(), getCookies(), contentString);
    }

    /**
     * Retrieves the content length of the request.
     *
     * @return the request content length
     */
    @Override
    public long getContentLength() {
        return exchange.getRequestContentLength();
    }

    /**
     * Retrieves the request character encoding.
     *
     * @return the request character encoding
     */
    @Override
    public String getCharacterEncoding() {
        return exchange.getRequestCharset();
    }

    /**
     * Retrieves the request content type. Generally this will only be present for requests with body content.
     *
     * @return the request content type
     */
    @Override
    public String getContentType() {
        final var header = exchange.getRequestHeaders().get(CONTENT_TYPE_HEADER);
        return header != null ? header.getFirst() : null;
    }
}