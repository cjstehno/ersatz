/**
 * Copyright (C) 2016 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.model;

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
 * Created by cjstehno on 12/2/16.
 */
public abstract class AbstractRequest implements Request {

    private final Map<String, List<String>> queryParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> cookies = new HashMap<>();
    private final List<Consumer<Request>> listeners = new LinkedList<>();
    private final List<Response> responses = new LinkedList<>();
    private final String path;
    private Function<Integer, Boolean> verifier = Verifiers.any();
    private int callCount;

    AbstractRequest(final String path) {
        this.path = path;
    }

    public Request header(final String name, final String value) {
        headers.put(name, value);
        return this;
    }

    public Request contentType(final String contentType) {
        header("Content-Type", contentType);
        return this;
    }

    public Request query(final String name, final String value) {
        queryParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    public Request cookie(final String name, final String value) {
        cookies.put(name, value);
        return this;
    }

    public Request listener(final Consumer<Request> listener) {
        listeners.add(listener);
        return this;
    }

    public Response responds() {
        Response response = new ResponseImpl();
        responses.add(response);
        return response;
    }

    public Request responder(final Consumer<Response> responder) {
        Response response = new ResponseImpl();
        responder.accept(response);
        responses.add(response);
        return this;
    }

    public Request verifier(final Function<Integer, Boolean> verifier) {
        this.verifier = verifier;
        return this;
    }

    boolean verify() {
        return verifier.apply(callCount);
    }

    boolean matches(final HttpServerExchange exchange) {
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

    // TODO: see if this can be package
    public void respond(final HttpServerExchange exchange) {
        ResponseImpl response = (ResponseImpl) responses.get(callCount >= responses.size() ? responses.size() - 1 : callCount);
        mark();

        response.send(exchange);
    }

    private void mark() {
        callCount++;

        for (final Consumer<Request> listener : listeners) {
            listener.accept(this);
        }
    }
}
