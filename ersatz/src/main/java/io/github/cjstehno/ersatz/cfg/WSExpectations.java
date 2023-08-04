/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.cfg;

import java.util.function.Consumer;

/**
 * Defines the expectations configuration available for websockets.
 */
public interface WSExpectations {

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection
     * will be expected in order for the verification to pass.
     *
     * @param path the request path
     * @return a reference to the web socket expectations
     */
    default WebSocketExpectations webSocket(String path) {
        return webSocket(path, null);
    }

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection
     * will be expected in order for the verification to pass.
     *
     * @param path   the request path
     * @param config the configuration consumer
     * @return a reference to the web socket expectations
     */
    WebSocketExpectations webSocket(String path, Consumer<WebSocketExpectations> config);
}
