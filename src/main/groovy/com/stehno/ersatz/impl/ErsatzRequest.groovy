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

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.Request
import com.stehno.ersatz.Response
import com.stehno.ersatz.Verifiers
import groovy.transform.CompileStatic

import java.util.function.Consumer
import java.util.function.Function

import static com.stehno.ersatz.Conditions.*

/**
 * <code>Request</code> implementation representing requests without body content.
 */
@CompileStatic
class ErsatzRequest implements Request {

    protected final Map<String, List<String>> queryParams = [:]
    protected final Map<String, String> headers = [:]
    protected final Map<String, String> cookies = [:]
    protected final List<Function<ClientRequest, Boolean>> conditions = []

    private final List<Consumer<Request>> listeners = []
    private final List<Response> responses = []
    private final boolean emptyResponse
    private final String path
    private final String method
    private Function<Integer, Boolean> verifier = Verifiers.any()
    private int callCount

    /**
     * Creates a new request with the specified method, path and optional empty response flag (defaults to false).
     *
     * @param method the request method
     * @param path the request path
     * @param emptyResponse whether or not this is a request with an empty response (defaults to false)
     */
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

    @Override
    Request header(final String name, final String value) {
        headers[name] = value
        this
    }

    @Override
    String getHeader(final String name) {
        headers[name]
    }

    @Override
    Request query(final String name, final String value) {
        queryParams.computeIfAbsent(name) { k -> [] }.add value
        this
    }

    @Override
    List<String> getQuery(final String name) {
        (queryParams[name] ?: []).asImmutable()
    }

    @Override
    Request cookie(final String name, final String value) {
        cookies[name] = value
        this
    }

    @Override
    String getCookie(final String name) {
        cookies[name]
    }

    @Override
    Request listener(final Consumer<Request> listener) {
        listeners.add(listener)
        this
    }

    @Override
    Response responds() {
        Response response = newResponse()
        responses.add(response)
        response
    }

    @Override
    Request responder(final Consumer<Response> responder) {
        Response response = newResponse()
        responder.accept(response)
        responses.add(response)
        this
    }

    @Override
    Request responder(@DelegatesTo(Response) final Closure closure) {
        Response response = newResponse()
        closure.setDelegate(response)
        closure.call()

        responses.add(response)

        this
    }

    @Override
    Request condition(final Function<ClientRequest, Boolean> matcher) {
        conditions.add(matcher)
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request conditions(List<Function<ClientRequest, Boolean>> matchers) {
        conditions.addAll(matchers)
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request verifier(final Function<Integer, Boolean> verifier) {
        this.verifier = verifier
        this
    }

    /**
     * Used to verify that the request has been called the expected number of times. By default there is no verification criteria, they must be
     * configured using the <code>verifier</code> methods.
     *
     * @return true if the call count matches the expected verification criteria
     */
    boolean verify() {
        verifier.apply(callCount)
    }

    /**
     * Used to determine whether or not the incoming client request matches this configured request. If there are configured <code>conditions</code>,
     * they will override the default match conditions (except for path and request method matching, and only those configured conditions will be
     * applied. The default conditions may be added back in using the <code>Conditions</code> functions.
     *
     * The default match criteria are:
     *
     * <ul>
     *  <li>The request methods must match.</li>
     *  <li>The request paths must match.</li>
     *  <li>The request query parameters must match (inclusive).</li>
     *  <li>The incoming request headers must contain all of the configured headers (non-inclusive).</li>
     *  <li>The incoming request cookies must contain all of the configured cookies (non-inclusive).</li>
     * </ul>
     *
     * @param clientRequest the incoming client request
     * @return true if the incoming request matches the configured request
     */
    boolean matches(final ClientRequest clientRequest) {
        if (conditions) {
            return methodEquals(this.method).apply(clientRequest) &&
                pathEquals(this.path).apply(clientRequest) &&
                conditions.every { it.apply(clientRequest) }

        }

        return methodEquals(this.method).apply(clientRequest) &&
            pathEquals(this.path).apply(clientRequest) &&
            queriesEquals(this.queryParams).apply(clientRequest) &&
            headersContains(this.headers).apply(clientRequest) &&
            cookiesContains(this.cookies).apply(clientRequest)
    }

    private Response newResponse() {
        new ErsatzResponse(emptyResponse)
    }

    /**
     * Used to retrieve the current response in the response list (based on the call count). The last response in the list will be sent to all future
     * calls.
     *
     * @return the current response
     */
    Response getCurrentResponse() {
        int index = callCount >= responses.size() ? responses.size() - 1 : callCount
        responses.get(index)
    }

    /**
     * Used to mark the request as having been called. Any configured listeners will be called after the call count has been incremented.
     */
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