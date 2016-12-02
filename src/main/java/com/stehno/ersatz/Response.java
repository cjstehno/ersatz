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
import io.undertow.util.HttpString;

import java.util.HashMap;
import java.util.Map;

public class Response {

    private final Map<String, String> headers = new HashMap<>();
    private Object body;
    private Integer code = 200;

    Response body(final Object content) {
        this.body = content;
        return this;
    }

    // TODO: support for more complex headers
    Response header(final String name, final String value) {
        headers.put(name, value);
        return this;
    }

    Response code(int code){
        this.code = code;
        return this;
    }

    void send(final HttpServerExchange exchange) {
        exchange.setStatusCode(code);

        headers.entrySet().forEach(entry -> exchange.getResponseHeaders().put(new HttpString(entry.getKey()), entry.getValue()));

        exchange.getResponseSender().send(body.toString());
    }
}
