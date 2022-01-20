/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.server;

/**
 * Defines the access points into the underlying HTTP server implementation.
 */
public interface UnderlyingServer {

    /**
     * The response body returned when no matching expectation could be found.
     */
    String NOT_FOUND_BODY = "404: Not Found";

    /**
     * Used to start the server, if not already started.
     */
    void start();

    /**
     * Used to stop the server, if it has been started.
     */
    void stop();

    /**
     * Used to retrieve the actual applied HTTP port for the server.
     *
     * @return the HTTP port
     */
    int getActualHttpPort();

    /**
     * Used to retrieve the actual applied HTTPS port for the server.
     *
     * @return the HTTPS port
     */
    int getActualHttpsPort();
}
