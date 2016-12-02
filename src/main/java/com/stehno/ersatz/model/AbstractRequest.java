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

import com.stehno.ersatz.Request;
import io.undertow.server.HttpServerExchange;

/**
 * Created by cjstehno on 12/2/16.
 */
public abstract class AbstractRequest implements Request {

    public abstract boolean verify();

    protected abstract boolean matches(final HttpServerExchange exchange);

    public abstract void respond(final HttpServerExchange exchange);
}
