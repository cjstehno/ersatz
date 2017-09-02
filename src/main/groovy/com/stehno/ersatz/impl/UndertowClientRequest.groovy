/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.HttpMethod
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import io.undertow.io.Receiver
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.Cookie
import io.undertow.util.HeaderMap

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER

/**
 *  The primary <code>ClientRequest</code> implementation used to wrap and expose the important parts of the underlying Undertow request context.
 */
@CompileStatic @TupleConstructor
class UndertowClientRequest implements ClientRequest {

    /**
     * The wrapped <code>HttpServerExchange</code> object.
     */
    final HttpServerExchange exchange

    /**
     * Used to retrieve the request protocol, generally HTTP or HTTPS.
     *
     * @return the request protocol
     */
    String getProtocol() {
        exchange.requestScheme
    }

    /**
     * Retrieves the HTTP method for the request.
     *
     * @return the HTTP method for the request
     */
    HttpMethod getMethod() {
        HttpMethod.valueOf(exchange.requestMethod.toString())
    }

    /**
     * Retrieves the request path.
     *
     * @return the request path
     */
    String getPath() {
        exchange.requestPath
    }

    /**
     * Retrieves the URL query string parameters for the request.
     *
     * @return the query string parameters
     */
    Map<String, Deque<String>> getQueryParams() {
        exchange.queryParameters
    }

    /**
     * Retrieves the request headers.
     *
     * @return the request headers
     */
    HeaderMap getHeaders() {
        exchange.requestHeaders
    }

    /**
     * Retrieves the cookies associated with the request.
     *
     * @return the request cookies
     */
    Map<String, Cookie> getCookies() {
        exchange.requestCookies
    }

    /**
     * Retrieves the body content (if any) as a byte array (null for an empty request).
     *
     * @return the optional body content as a byte array.
     */
    @Memoized
    byte[] getBody() {
        byte[] bytes = null

        exchange.requestReceiver.receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            void handle(final HttpServerExchange exch, byte[] message) {
                bytes = message
            }
        })

        bytes
    }

    @Override
    String toString() {
        "{ $method $path (query=$queryParams, headers=$headers, cookies=$cookies): ${body ? new String(body).take(1000) : '<empty>'} }"
    }

    /**
     * Retrieves the content length of the request.
     *
     * @return the request content length
     */
    @Override
    long getContentLength() {
        exchange.requestContentLength
    }

    /**
     * Retrieves the request character encoding.
     *
     * @return the request character encoding
     */
    @Override
    String getCharacterEncoding() {
        exchange.requestCharset
    }

    /**
     * Retrieves the request content type. Generally this will only be present for requests with body content.
     *
     * @return the request content type
     */
    @Override
    String getContentType() {
        exchange.requestHeaders.get(CONTENT_TYPE_HEADER)?.first
    }
}