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

import com.stehno.ersatz.GetRequest;
import com.stehno.ersatz.Request;
import com.stehno.ersatz.Response;
import com.stehno.ersatz.Verifiers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by cjstehno on 12/1/16.
 */
public class GetRequestImpl extends AbstractRequest implements GetRequest {

    private final Map<String, List<String>> queryParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> cookies = new HashMap<>();
    private final List<Consumer<Request>> listeners = new LinkedList<>();
    private Function<Integer, Boolean> verifier = Verifiers.any();
    private final List<Response> responses = new LinkedList<>();
    private String path;
    private int callCount;

    GetRequestImpl(final String path) {
        this.path = path;
    }

    public GetRequest header(final String name, final String value) {
        headers.put(name, value);
        return this;
    }

    public GetRequest contentType(final String contentType) {
        header("Content-Type", contentType);
        return this;
    }

    public GetRequest query(final String name, final String value) {
        queryParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    public GetRequest cookie(final String name, final String value) {
        cookies.put(name, value);
        return this;
    }

    public GetRequest listener(final Consumer<Request> listener) {
        listeners.add(listener);
        return this;
    }

    // single response or will act as onRest
    public Response responds() {
        Response response = new ResponseImpl();
        responses.add(response);
        return response;
    }

    public GetRequest responder(final Consumer<Response> responder) {
        Response response = new ResponseImpl();
        responder.accept(response);
        responses.add(response);
        return this;
    }

    public GetRequestImpl verifier(final Function<Integer, Boolean> verifier) {
        this.verifier = verifier;
        return this;
    }

    public void respond(final HttpServerExchange exchange) {
        ResponseImpl response = (ResponseImpl) responses.get(callCount >= responses.size() ? responses.size() - 1 : callCount);
        mark();

        response.send(exchange);
    }

    public boolean verify() {
        return verifier.apply(callCount);
    }

    protected boolean matches(final HttpServerExchange exchange) {
        return exchange.getRequestPath().equals(path) &&
            matchQueryParams(exchange.getQueryParameters()) &&
            containsHeaders(exchange.getRequestHeaders()) &&
            containsCookies(exchange.getRequestCookies());
    }

    // header matching is not absolute - the request must contain the specified headers but not necessarily all of them
    // TODO: needs to support more complicated headers
    private boolean containsHeaders(final HeaderMap requestHeads) {
        return headers.entrySet().stream().allMatch(entry -> entry.getValue().equals(requestHeads.getFirst(entry.getKey())));
    }

    private boolean containsCookies(final Map<String, Cookie> requestCookies) {
        return cookies.entrySet().stream().allMatch(entry -> requestCookies.containsKey(entry.getKey()) && entry.getValue().equals(requestCookies.get(entry.getKey()).getValue()));
    }

    private boolean matchQueryParams(final Map<String, Deque<String>> requestQs) {
        boolean one = queryParams.entrySet().stream().allMatch(entry -> requestQs.containsKey(entry.getKey()) && requestQs.get(entry.getKey()).containsAll(entry.getValue()));
        boolean two = requestQs.entrySet().stream().allMatch(entry -> queryParams.containsKey(entry.getKey()) && queryParams.get(entry.getKey()).containsAll(entry.getValue()));
        return one && two;
    }

    private void mark() {
        callCount++;

        for (final Consumer<Request> listener : listeners) {
            listener.accept(this);
        }
    }
}
