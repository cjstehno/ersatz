package com.stehno.ersatz

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import io.undertow.io.Receiver
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.Cookie
import io.undertow.util.HeaderMap

import java.util.function.Function

/**
 * Created by cjstehno on 12/6/16.
 */
@CompileStatic @TupleConstructor
class ClientRequest {

    final HttpServerExchange exchange

    String getMethod() {
        exchange.requestMethod.toString()
    }

    String getPath() {
        exchange.requestPath
    }

    Map<String, Deque<String>> getQueryParams() {
        exchange.queryParameters
    }

    HeaderMap getHeaders() {
        exchange.requestHeaders
    }

    Map<String, Cookie> getCookies() {
        exchange.requestCookies
    }

    @Memoized
    byte[] getBodyAsBytes() {
        byte[] bytes = null

        exchange.requestReceiver.receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            void handle(final HttpServerExchange exch, byte[] message) {
                bytes = message
            }
        })

        bytes
    }

    @Memoized
    Object getBody(final Function<byte[], Object> converter = { b -> b }) {
        bodyAsBytes ? converter.apply(bodyAsBytes) : null
    }

    String getBodyAsString() {
        getBody({ b -> b ? new String(b as byte[]) : '' })
    }

    @Override
    String toString() {
        "{ $method $path (query=$queryParams, headers=$headers, cookies=$cookies): ${bodyAsString ?: '<empty>'} }"
    }
}
