/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.server.undertow;

import io.github.cjstehno.ersatz.cfg.Response;
import io.github.cjstehno.ersatz.server.ClientRequest;
import io.undertow.server.HttpServerExchange;

/**
 * Defines a more expressive custom handler for the Undertow implementation of the Ersatz server.
 */
public interface ErsatzHandler {

    /**
     * Handles the current request in the exchange, with the specified client request and configured response.
     *
     * @param exchange the server exchange object for the current request/response cycle
     * @param request the incoming client request
     * @param response the configured outgoing response
     * @throws Exception if there is a problem handling the request
     */
    void handleRequest(HttpServerExchange exchange, ClientRequest request, Response response) throws Exception;
}
