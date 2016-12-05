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

import com.stehno.ersatz.Expectations
import com.stehno.ersatz.Request
import com.stehno.ersatz.RequestWithContent
import groovy.transform.CompileStatic
import io.undertow.server.HttpServerExchange

import java.util.function.Consumer

/**
 * Implementation of the <code>Expectations</code> interface.
 */
@CompileStatic @SuppressWarnings('ConfusingMethodName')
class ExpectationsImpl implements Expectations {

    private static final String GET = 'GET'
    private static final String HEAD = 'HEAD'
    private static final String POST = 'POST'
    private static final String PUT = 'PUT'
    private static final String DELETE = 'DELETE'
    private static final String PATCH = 'PATCH'
    private final List<Request> requests = []

    Request get(final String path) {
        expect new ErsatzRequest(GET, path)
    }

    Request get(final String path, @DelegatesTo(Request) final Closure closure) {
        expect new ErsatzRequest(GET, path), closure
    }

    @Override
    Request get(String path, Consumer<Request> config) {
        expect new ErsatzRequest(GET, path), config
    }

    @Override
    Request head(String path) {
        expect new ErsatzRequest(HEAD, path, true)
    }

    @Override
    Request head(String path, @DelegatesTo(Request) Closure closure) {
        expect new ErsatzRequest(HEAD, path, true), closure
    }

    @Override
    Request head(String path, Consumer<Request> config) {
        expect new ErsatzRequest(HEAD, path), config
    }

    @Override
    RequestWithContent post(String path) {
        expect(new ErsatzRequestWithContent(POST, path)) as RequestWithContent
    }

    @Override
    RequestWithContent post(String path, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(POST, path), closure) as RequestWithContent
    }

    @Override
    RequestWithContent post(String path, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(POST, path), config) as RequestWithContent
    }

    @Override
    RequestWithContent put(String path) {
        expect(new ErsatzRequestWithContent(PUT, path)) as RequestWithContent
    }

    @Override
    RequestWithContent put(String path, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(PUT, path), closure) as RequestWithContent
    }

    @Override
    RequestWithContent put(String path, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PUT, path), config) as RequestWithContent
    }

    @Override
    Request delete(String path) {
        expect new ErsatzRequest(DELETE, path)
    }

    @Override
    Request delete(String path, @DelegatesTo(Request) Closure closure) {
        expect new ErsatzRequest(DELETE, path), closure
    }

    @Override
    Request delete(String path, Consumer<Request> config) {
        expect new ErsatzRequest(DELETE, path), config
    }

    @Override
    RequestWithContent patch(String path) {
        expect(new ErsatzRequestWithContent(PATCH, path)) as RequestWithContent
    }

    @Override
    RequestWithContent patch(String path, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(PATCH, path), closure) as RequestWithContent
    }

    @Override
    RequestWithContent patch(String path, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PATCH, path), config) as RequestWithContent
    }

    Request findMatch(final HttpServerExchange exchange) {
        ClientRequestMatcher matcher = new ClientRequestMatcher(exchange)
        requests.find { r -> ((ErsatzRequest) r).matches(matcher) }
    }

    boolean verify() {
        requests.each { r ->
            assert ((ErsatzRequest) r).verify(), "Expectations for $r were not met."
        }
        true
    }

    private Request expect(final Request request) {
        requests.add(request)
        request
    }

    private Request expect(final Request request, final Closure closure) {
        closure.setDelegate(request)
        closure.call()

        requests.add(request)
        request
    }

    private Request expect(final Request request, final Consumer<? extends Request> consumer) {
        consumer.accept(request)
        requests.add(request)
        request
    }
}
