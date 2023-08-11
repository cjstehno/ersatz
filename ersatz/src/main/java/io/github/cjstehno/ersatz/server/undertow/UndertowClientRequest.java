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

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.server.ClientRequest;
import io.undertow.server.HttpServerExchange;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.cjstehno.ersatz.cfg.ContentType.CONTENT_TYPE_HEADER;
import static io.undertow.util.QueryParameterUtils.parseQueryString;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;

/**
 * The primary <code>ClientRequest</code> implementation used to wrap and expose the important parts of the underlying Undertow request context.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UndertowClientRequest implements ClientRequest {

    private final HttpServerExchange exchange;
    private AtomicReference<byte[]> content;

    /**
     * Used to retrieve the request scheme, generally HTTP or HTTPS.
     *
     * @return the request scheme
     */
    @Override public String getScheme() {
        return exchange.getRequestScheme();
    }

    /**
     * Retrieves the HTTP method for the request.
     *
     * @return the HTTP method for the request
     */
    @Override public HttpMethod getMethod() {
        return HttpMethod.valueOf(exchange.getRequestMethod().toString());
    }

    /**
     * Retrieves the request path.
     *
     * @return the request path
     */
    @Override public String getPath() {
        return exchange.getRequestPath();
    }

    /**
     * Retrieves the URL query string parameters for the request.
     *
     * @return the query string parameters
     */
    @Override public Map<String, Deque<String>> getQueryParams() {
        return exchange.getQueryParameters();
    }

    /**
     * Retrieves the request headers.
     *
     * @return the request headers
     */
    @Override public Map<String, Deque<String>> getHeaders() {
        final var map = new LinkedHashMap<String, Deque<String>>();

        exchange.getRequestHeaders().forEach(header -> {
            map.computeIfAbsent(header.getHeaderName().toString(), s -> new ArrayDeque<>()).addAll(header);
        });

        return map;
    }

    /**
     * Retrieves the cookies associated with the request.
     *
     * @return the request cookies
     */
    @Override public Map<String, Cookie> getCookies() {
        val cookies = new LinkedHashMap<String, Cookie>();

        exchange.requestCookies().forEach(c -> {
            cookies.put(
                c.getName(),
                new Cookie(
                    c.getValue(),
                    c.getComment(),
                    c.getDomain(),
                    c.getPath(),
                    c.getVersion(),
                    c.isHttpOnly(),
                    c.getMaxAge(),
                    c.isSecure()
                )
            );
        });

        return cookies;
    }

    /**
     * Retrieves the body content (if any) as a byte array (null for an empty request).
     *
     * @return the optional body content as a byte array.
     */
    @Override public byte[] getBody() {
        if (content == null) {
            content = new AtomicReference<>();
            exchange.getRequestReceiver().receiveFullBytes((exch, message) -> content.set(message));
        }

        return content.get();
    }

    @Override public Map<String, Deque<String>> getBodyParameters() {
        final var body = getBody();
        return body != null ? parseQueryString(new String(body, UTF_8), UTF_8.displayName()) : emptyMap();
    }

    @Override
    public String toString() {
        String contentString = "<empty>";

        final var body = getBody();
        if (body != null && getContentType() != null && getContentType().startsWith("text/")) {
            contentString = new String(body, UTF_8);
        } else if (body != null) {
            contentString = format("<%d of %s content>", getContentLength(), getContentType());
        }

        return format(
            "{ %s %s(query=%s, headers=%s, cookies=%s): %s }",
            getMethod(), getPath(), getQueryParams(), getHeaders(), getCookies(), contentString
        );
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