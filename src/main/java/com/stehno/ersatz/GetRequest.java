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
package com.stehno.ersatz;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;

import java.util.*;
import java.util.function.Supplier;

/**
 * Created by cjstehno on 12/1/16.
 */
public class GetRequest {

    private final Map<String, List<String>> queryParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private String path;
    private Response response;
    private int callCount;
    private Supplier<Boolean> verificationFn = () -> true;

    public GetRequest(final String path) {
        this.path = path;
    }

    GetRequest header(final String name, final String value) {
        headers.put(name, value);
        return this;
    }

    GetRequest query(final String name, final String value) {
        queryParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    void countCall() {
        callCount++;
    }

    Response responds() {
        response = new Response();
        return response;
    }

    public Response getResponse() {
        return response;
    }

    boolean verify() {
        return verificationFn.get();
    }

    GetRequest atLeast(final int expectedCount) {
        verificationFn = () -> callCount >= expectedCount;
        return this;
    }

    // TODO: should this return a Predicate for use in the filter?
    boolean matches(final HttpServerExchange exchange) {
        return exchange.getRequestPath().equals(path) && matchQueryParams(exchange.getQueryParameters()) && matchHeaders(exchange.getRequestHeaders());
    }

    private boolean matchHeaders(final HeaderMap requestHeads){
        headers are a pain in the butt - working here.
    }

    private boolean matchQueryParams(final Map<String, Deque<String>> requestQs) {
        boolean one = queryParams.entrySet().stream().allMatch(entry -> requestQs.containsKey(entry.getKey()) && requestQs.get(entry.getKey()).containsAll(entry.getValue()));
        boolean two = requestQs.entrySet().stream().allMatch(entry -> queryParams.containsKey(entry.getKey()) && queryParams.get(entry.getKey()).containsAll(entry.getValue()));
        return one && two;
    }
}
