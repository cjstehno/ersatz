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

import com.stehno.ersatz.Expectations;
import com.stehno.ersatz.GetRequest;
import com.stehno.ersatz.Request;
import io.undertow.server.HttpServerExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by cjstehno on 12/1/16.
 */
public class ExpectationsImpl implements Expectations {

    private final List<Request> requests = new ArrayList<>();

    public GetRequest get(final String path) {
        GetRequest request = new GetRequestImpl(path);
        requests.add(request);
        return request;
    }

    // PostRequest post(final String path){}...

    public Optional<Request> find(final HttpServerExchange exchange) {
        return requests.stream().filter(request -> ((AbstractRequest) request).matches(exchange)).findFirst();
    }

    public boolean verify() {
        return requests.stream().allMatch(request -> ((AbstractRequest) request).verify());
    }
}
