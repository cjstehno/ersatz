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
package com.stehno.ersatz.impl

import com.stehno.ersatz.Request
import com.stehno.ersatz.Response
import com.stehno.ersatz.Verifiers
import groovy.transform.CompileStatic
import io.undertow.server.HttpServerExchange

import java.util.function.Consumer
import java.util.function.Function

import static com.stehno.ersatz.Conditions.*
import static groovy.lang.Closure.DELEGATE_ONLY
import static groovy.transform.TypeCheckingMode.SKIP

/**
 * <code>Request</code> implementation representing requests without body content.
 */
@CompileStatic
class ErsatzRequest implements Request {

    protected final Map<String, List<String>> queryParams = [:]
    protected final Map<String, String> headers = [:]
    protected final Map<String, String> cookies = [:]
    private final List<Consumer<Request>> listeners = []
    private final List<Response> responses = []
    protected final List<Function<HttpServerExchange, Boolean>> conditions = []
    private final boolean emptyResponse
    private final String path
    private final String method
    private Function<Integer, Boolean> verifier = Verifiers.any()
    private int callCount

    ErsatzRequest(final String method, final String path, final boolean emptyResponse = false) {
        this.method = method
        this.path = path
        this.emptyResponse = emptyResponse
    }

    @Override
    String getPath() {
        path
    }

    @Override
    String getMethod() {
        method
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request headers(final Map<String, String> heads) {
        headers.putAll(heads)
        this
    }

    @Override
    Request queries(final Map<String, List<String>> map) {
        map.each { k, v ->
            if (queryParams.containsKey(k)) {
                queryParams[k].addAll(v)
            } else {
                queryParams[k] = v
            }
        }
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request cookies(Map<String, String> cookies) {
        this.cookies.putAll(cookies)
        this
    }

    Request header(final String name, final String value) {
        headers[name] = value
        this
    }

    String getHeader(final String name) {
        headers[name]
    }

    Request query(final String name, final String value) {
        queryParams.computeIfAbsent(name) { k -> [] }.add value
        this
    }

    List<String> getQuery(final String name) {
        (queryParams[name] ?: []).asImmutable()
    }

    Request cookie(final String name, final String value) {
        cookies[name] = value
        this
    }

    String getCookie(final String name) {
        cookies[name]
    }

    Request listener(final Consumer<Request> listener) {
        listeners.add(listener)
        this
    }

    Response responds() {
        Response response = newResponse()
        responses.add(response)
        response
    }

    Request responder(final Consumer<Response> responder) {
        Response response = newResponse()
        responder.accept(response)
        responses.add(response)
        this
    }

    Request responder(@DelegatesTo(Response) final Closure closure) {
        Response response = newResponse()
        closure.setDelegate(response)
        closure.call()

        responses.add(response)

        this
    }

    // TODO: when conditions applied they override existing - only specified conditions will be applied (except method and path)
    Request condition(final Function<HttpServerExchange, Boolean> matcher) {
        conditions.add(matcher)
        this
    }

    @SuppressWarnings('ConfusingMethodName')
    Request conditions(List<Function<HttpServerExchange, Boolean>> matchers) {
        conditions.addAll(matchers)
        this
    }

    @SuppressWarnings('ConfusingMethodName')
    Request verifier(final Function<Integer, Boolean> verifier) {
        this.verifier = verifier
        this
    }

    boolean verify() {
        verifier.apply(callCount)
    }

    @CompileStatic(SKIP) // TODO: I would still like to abstract the HttpServerExchange away to something less impl-specific
    boolean matches(final HttpServerExchange exchange) {
        if (conditions) {
            return methodEquals(this.method).apply(exchange) &&
                pathEquals(this.path).apply(exchange) &&
                conditions.every { it.apply(exchange) }

        }

        return methodEquals(this.method).apply(exchange) &&
            pathEquals(this.path).apply(exchange) &&
            queriesEquals(this.queryParams).apply(exchange) &&
            headersContains(this.headers).apply(exchange) &&
            cookiesContains(this.cookies).apply(exchange)
    }

    protected Response newResponse() {
        new ErsatzResponse(emptyResponse)
    }

    Response getCurrentResponse() {
        int index = callCount >= responses.size() ? responses.size() - 1 : callCount
        responses.get(index)
    }

    void mark() {
        callCount++

        for (final Consumer<Request> listener : listeners) {
            listener.accept(this)
        }
    }

    @Override String toString() {
        "{ $method $path (query=${queryParams}, headers=$headers, cookies=$cookies): counted=$callCount }"
    }
}