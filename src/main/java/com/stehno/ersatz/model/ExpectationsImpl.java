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
package com.stehno.ersatz.model;

import com.stehno.ersatz.ContentRequest;
import com.stehno.ersatz.Expectations;
import com.stehno.ersatz.Request;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.undertow.server.HttpServerExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the `Expectations` interface.
 */
public class ExpectationsImpl implements Expectations {

    private final List<Request> requests = new ArrayList<>();

    public Request get(final String path) {
        return addRequest(new GetRequest(path));
    }

    public Request get(final String path, @DelegatesTo(Request.class) final Closure closure) {
        return addRequest(new GetRequest(path), closure);
    }

    @Override
    public Request head(String path) {
        return addRequest(new HeadRequest(path));
    }

    @Override
    public Request head(String path, @DelegatesTo(Request.class) Closure closure) {
        return addRequest(new HeadRequest(path), closure);
    }

    @Override
    public ContentRequest post(String path) {
        return addRequest(new PostRequest(path));
    }

    @Override
    public ContentRequest post(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        return addRequest(new PostRequest(path), closure);
    }

    @Override
    public ContentRequest put(String path) {
        return addRequest(new PutRequest(path));
    }

    @Override
    public ContentRequest put(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        return addRequest(new PutRequest(path), closure);
    }

    @Override
    public Request delete(String path) {
        return addRequest(new DeleteRequest(path));
    }

    @Override
    public Request delete(String path, @DelegatesTo(Request.class) Closure closure) {
        return addRequest(new DeleteRequest(path), closure);
    }

    @Override
    public ContentRequest patch(String path) {
        return addRequest(new PatchRequest(path));
    }

    @Override
    public ContentRequest patch(String path, @DelegatesTo(ContentRequest.class) Closure closure) {
        return addRequest(new PatchRequest(path), closure);
    }

    public Optional<Request> find(final HttpServerExchange exchange) {
        return requests.stream().filter(request -> ((AbstractRequest) request).matches(exchange)).findFirst();
    }

    public boolean verify() {
        return requests.stream().allMatch(request -> ((AbstractRequest) request).verify());
    }

    private <R extends Request> R addRequest(final Request request) {
        requests.add(request);
        return (R) request;
    }

    private <R extends Request> R addRequest(final Request request, final Closure closure) {
        closure.setDelegate(request);
        closure.call();

        requests.add(request);
        return (R) request;
    }
}
