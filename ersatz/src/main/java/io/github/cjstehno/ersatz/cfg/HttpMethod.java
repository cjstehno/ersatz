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

/**
 * Enumeration of the supported HTTP request methods.
 */
public enum HttpMethod {

    /**
     * Wildcard matching any HTTP method.
     */
    ANY("*"),

    /**
     * HTTP GET method matcher.
     */
    GET("GET"),

    /**
     * HTTP HEAD method matcher.
     */
    HEAD("HEAD"),

    /**
     * HTTP POST method matcher.
     */
    POST("POST"),

    /**
     * HTTP PUT method matcher.
     */
    PUT("PUT"),

    /**
     * HTTP DELETE method matcher.
     */
    DELETE("DELETE"),

    /**
     * HTTP PATCH method matcher.
     */
    PATCH("PATCH"),

    /**
     * HTTP OPTIONS method matcher.
     */
    OPTIONS("OPTIONS"),

    /**
     * HTTP TRACE method matcher.
     */
    TRACE("TRACE");

    private final String value;

    HttpMethod(final String value) {
        this.value = value;
    }

    /**
     * Retrieve the text value for the method.
     *
     * @return the method label.
     */
    public String getValue() {
        return value;
    }

    @Override public String toString() {
        return value;
    }
}