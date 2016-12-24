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
import org.hamcrest.Matcher
import org.hamcrest.StringDescription

import java.util.function.Consumer

import static org.hamcrest.Matchers.*

/**
 * <code>Request</code> implementation representing requests without body content.
 */
class ErsatzRequest implements Request {

    protected static final String GET = 'GET'
    protected static final String HEAD = 'HEAD'
    protected static final String POST = 'POST'
    protected static final String PUT = 'PUT'
    protected static final String DELETE = 'DELETE'
    protected static final String PATCH = 'PATCH'

    private final List<RequestMatcher> matchers = []
    private final List<Consumer<ClientRequest>> listeners = []
    private final List<Response> responses = []
    private final boolean emptyResponse
    private Matcher<?> callVerifier = anything()
    private int callCount

    /**
     * Creates a new request with the specified method, path matcher and optional empty response flag (defaults to false).
     *
     * @param method the request method
     * @param pathMatcher the path matcher
     * @param emptyResponse whether or not this is a request with an empty response (defaults to false)
     */
    ErsatzRequest(final String method, final Matcher<String> pathMatcher, final boolean emptyResponse = false) {
        matchers << RequestMatcher.method(equalTo(method))
        matchers << RequestMatcher.path(pathMatcher)
        this.emptyResponse = emptyResponse
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request headers(final Map<String, Object> heads) {
        heads.each { k, v ->
            header k, v
        }
        this
    }

    @Override
    Request header(final String name, final String value) {
        header name, equalTo(value)
    }

    @Override
    Request header(final String name, final Matcher<String> value) {
        matchers << RequestMatcher.header(name, value)
        this
    }

    @Override
    Request query(final String name, final String value) {
        query name, contains(value)
    }

    @Override
    Request query(final String name, final Iterable<String> value) {
        query name, containsInAnyOrder((value as Collection<String>).collect { equalTo(it) })
    }

    @Override
    Request query(final String name, final Matcher<Iterable<String>> matcher) {
        matchers << RequestMatcher.query(name, matcher)
        this
    }

    @Override
    Request queries(final Map<String, Object> map) {
        map.each { k, v ->
            query k, v
        }
        this
    }

    @Override
    Request cookie(final String name, final String value) {
        cookie name, equalTo(value)
    }

    @Override
    Request cookie(final String name, final Matcher<String> value) {
        matchers << RequestMatcher.cookie(name, value)
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request cookies(Map<String, Object> cookies) {
        cookies.each { k, v ->
            cookie k, v
        }
        this
    }

    @Override
    Request listener(final Consumer<ClientRequest> listener) {
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

    @Override @SuppressWarnings('ConfusingMethodName')
    Request called(final Matcher<Integer> callVerifier) {
        this.callVerifier = callVerifier
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request called(final int count) {
        called equalTo(count)
    }

    /**
     * Used to verify that the request has been called the expected number of times. By default there is no verification criteria, they must be
     * configured using one of the <code>called()</code> methods.
     *
     * @return true if the call count matches the expected verification criteria
     */
    boolean verify() {
        callVerifier.matches(callCount)
    }

    /**
     * Used to determine whether or not the incoming client request matches this configured request. All configured matchers must return
     * <code>true</code> in order for the match to be successful. By default, all request have a matcher for request method and request path, the
     * others are optional.
     *
     * @param clientRequest the incoming client request
     * @return true if the incoming request matches the configured request
     */
    boolean matches(final ClientRequest clientRequest) {
        matchers.every { m ->
            m.matches(clientRequest)
        }
    }

    protected void addMatcher(final RequestMatcher matcher) {
        matchers << matcher
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
        index >= 0 ? responses[index] : null
    }

    /**
     * Used to mark the request as having been called. Any configured listeners will be called after the call count has been incremented.
     */
    void mark(final ClientRequest cr) {
        callCount++

        for (final Consumer<ClientRequest> listener : listeners) {
            listener.accept(cr)
        }
    }

    @Override String toString() {
        StringBuilder str = new StringBuilder()
        str.append "Expectations (${getClass().simpleName}): "

        matchers.each { m ->
            StringDescription desc = new StringDescription()
            m.matcher.describeTo(desc)
            str.append(desc).append(', ')
        }

        str.toString()
    }
}