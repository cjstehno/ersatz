/**
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
package com.stehno.ersatz.model

import com.stehno.ersatz.ContentRequest
import com.stehno.ersatz.Expectations
import com.stehno.ersatz.Request
import groovy.transform.CompileStatic
import io.undertow.server.HttpServerExchange

/**
 * Implementation of the <code>Expectations</code> interface.
 */
@CompileStatic
class ExpectationsImpl implements Expectations {

    private final List<Request> requests = new ArrayList<>()

    Request get(final String path) {
        expect new GetRequest(path)
    }

    Request get(final String path, @DelegatesTo(Request.class) final Closure closure) {
        expect new GetRequest(path), closure
    }

    @Override
    Request head(String path) {
        expect new HeadRequest(path)
    }

    @Override
    Request head(String path, @DelegatesTo(Request.class) Closure closure) {
        expect new HeadRequest(path), closure
    }

    @Override
    ContentRequest post(String path) {
        expect(new PostRequest(path)) as ContentRequest
    }

    @Override
    ContentRequest post(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        expect(new PostRequest(path), closure) as ContentRequest
    }

    @Override
    ContentRequest put(String path) {
        expect(new PutRequest(path)) as ContentRequest
    }

    @Override
    ContentRequest put(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        expect(new PutRequest(path), closure) as ContentRequest
    }

    @Override
    Request delete(String path) {
        expect new DeleteRequest(path)
    }

    @Override
    Request delete(String path, @DelegatesTo(Request.class) Closure closure) {
        expect new DeleteRequest(path), closure
    }

    @Override
    ContentRequest patch(String path) {
        expect(new PatchRequest(path)) as ContentRequest
    }

    @Override
    ContentRequest patch(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        expect(new PatchRequest(path), closure) as ContentRequest
    }

    Request findMatch(final HttpServerExchange exchange) {
        requests.find { r -> ((AbstractRequest) r).matches(exchange) }
    }

    boolean verify() {
        requests.every { r -> ((AbstractRequest) r).verify() }
    }

    private Request expect(final Request request) {
        requests.add(request)
        return request
    }

    private Request expect(final Request request, final Closure closure) {
        closure.setDelegate(request)
        closure.call()

        requests.add(request)
        return request
    }
}
