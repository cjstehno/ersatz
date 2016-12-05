package com.stehno.ersatz.impl

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.undertow.io.Receiver
import io.undertow.server.HttpServerExchange

import java.util.function.Function

/**
 * Abstraction of the server request object used to perform matching operations against the request expectations.
 *
 * Headers and Cookies are matched with a "contains" strategy, meaning that as long as the expected items are in the request, the match is accepted,
 * while the query strings, method name, path, and body are matched for equivalence.
 */
@CompileStatic @TupleConstructor
class ClientRequestMatcher {

    final HttpServerExchange exchange

    boolean method(final String method) {
        exchange.requestMethod.toString().equalsIgnoreCase(method)
    }

    boolean path(final String path) {
        exchange.requestPath == path
    }

    boolean queries(final Map<String, List<String>> queries) {
        queries.every { k, v ->
            exchange.queryParameters.containsKey(k) && exchange.queryParameters.get(k).containsAll(v)
        } && exchange.queryParameters.every { k, v ->
            queries.containsKey(k) && queries.get(k).containsAll(v)
        }
    }

    boolean headers(final Map<String, String> headers) {
        headers.every { k, v -> v == exchange.requestHeaders.getFirst(k) }
    }

    boolean cookies(final Map<String, String> cookies) {
        cookies.every { k, v -> exchange.requestCookies.containsKey(k) && v == exchange.requestCookies.get(k).value }
    }

    boolean body(final Object body, final Function<byte[], Object> converter) {
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
