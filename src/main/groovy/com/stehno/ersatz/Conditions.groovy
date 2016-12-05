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

import groovy.transform.CompileStatic
import io.undertow.io.Receiver
import io.undertow.server.HttpServerExchange

import java.util.function.Function

@CompileStatic
class Conditions {

    /*
* Headers and Cookies are matched with a "contains" strategy, meaning that as long as the expected items are in the request, the match is accepted,
 * while the query strings, method name, path, and body are matched for equivalence.
     */

    static final Function<HttpServerExchange, Boolean> methodEquals(final String method) {
        return new Function<HttpServerExchange, Boolean>() {
            @Override Boolean apply(final HttpServerExchange exchange) {
                exchange.requestMethod.toString().equalsIgnoreCase(method)
            }
        }
    }

    static final Function<HttpServerExchange, Boolean> pathEquals(final String path) {
        return new Function<HttpServerExchange, Boolean>() {
            @Override Boolean apply(final HttpServerExchange exchange) {
                exchange.requestPath == path
            }
        }
    }

    static final Function<HttpServerExchange, Boolean> queriesEquals(final Map<String, List<String>> queries) {
        return new Function<HttpServerExchange, Boolean>() {
            @Override Boolean apply(final HttpServerExchange exchange) {
                queries.every { k, v ->
                    exchange.queryParameters.containsKey(k) && exchange.queryParameters.get(k).containsAll(v)
                } && exchange.queryParameters.every { k, v ->
                    queries.containsKey(k) && queries.get(k).containsAll(v)
                }
            }
        }
    }

    static final Function<HttpServerExchange, Boolean> headersContains(final Map<String, String> headers) {
        return new Function<HttpServerExchange, Boolean>() {
            @Override Boolean apply(final HttpServerExchange exchange) {
                headers.every { k, v -> v == exchange.requestHeaders.getFirst(k) }
            }
        }
    }

    static final Function<HttpServerExchange, Boolean> cookiesContains(final Map<String, String> cookies) {
        return new Function<HttpServerExchange, Boolean>() {
            @Override Boolean apply(final HttpServerExchange exchange) {
                cookies.every { k, v -> exchange.requestCookies.containsKey(k) && v == exchange.requestCookies.get(k).value }
            }
        }
    }

    static final Function<HttpServerExchange, Boolean> bodyEquals(final Object body, final Function<byte[], Object> converter) {
        return new Function<HttpServerExchange, Boolean>() {
            @Override Boolean apply(final HttpServerExchange exchange) {
                boolean match = false

                exchange.requestReceiver.receiveFullBytes(new Receiver.FullBytesCallback() {
                    @Override
                    void handle(final HttpServerExchange exch, byte[] message) {
                        match = converter.apply(message) == body
                    }
                })

                match
            }
        }
    }
}
