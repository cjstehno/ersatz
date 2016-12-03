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
 * Implementation of the `Expectations` interface.
 */
@CompileStatic
class ExpectationsImpl implements Expectations {

    private final List<Request> requests = new ArrayList<>()

    Request get(final String path) {
        addRequest(new GetRequest(path))
    }

    Request get(final String path, @DelegatesTo(Request.class) final Closure closure) {
        addRequest(new GetRequest(path), closure)
    }

    @Override
    Request head(String path) {
        addRequest(new HeadRequest(path))
    }

    @Override
    Request head(String path, @DelegatesTo(Request.class) Closure closure) {
        addRequest(new HeadRequest(path), closure)
    }

    @Override
    ContentRequest post(String path) {
        addRequest(new PostRequest(path)) as ContentRequest
    }

    @Override
    ContentRequest post(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        addRequest(new PostRequest(path), closure) as ContentRequest
    }

    @Override
    ContentRequest put(String path) {
        addRequest(new PutRequest(path)) as ContentRequest
    }

    @Override
    ContentRequest put(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        addRequest(new PutRequest(path), closure) as ContentRequest
    }

    @Override
    Request delete(String path) {
        addRequest(new DeleteRequest(path))
    }

    @Override
    Request delete(String path, @DelegatesTo(Request.class) Closure closure) {
        addRequest(new DeleteRequest(path), closure)
    }

    @Override
    ContentRequest patch(String path) {
        addRequest(new PatchRequest(path)) as ContentRequest
    }

    @Override
    ContentRequest patch(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        addRequest(new PatchRequest(path), closure) as ContentRequest
    }

    Request find(final HttpServerExchange exchange) {
        requests.find { r -> ((AbstractRequest) r).matches(exchange) }
    }

    boolean verify() {
        requests.every { r -> ((AbstractRequest) r).verify() }
    }

    private Request addRequest(final Request request) {
        requests.add(request)
        return request
    }

    private Request addRequest(final Request request, final Closure closure) {
        closure.setDelegate(request)
        closure.call()

        requests.add(request)
        return request
    }
}
