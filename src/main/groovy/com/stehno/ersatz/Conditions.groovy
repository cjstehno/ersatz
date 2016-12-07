/*
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
package com.stehno.ersatz

import groovy.transform.CompileStatic

import java.util.function.Function

/**
 * A handful of request matching conditions. These are used internally by the request, when not overridden. These functions may also be used in the
 * <code>Request</code> <code>condition</code> methods.
 */
@CompileStatic
class Conditions {

    /*
* Headers and Cookies are matched with a "contains" strategy, meaning that as long as the expected items are in the request, the match is accepted,
 * while the query strings, method name, path, and body are matched for equivalence.
     */

    /**
     * Provides a function that will return true if the request method equals the specified method.
     *
     * @param method the expected request method
     * @return true if the methods match
     */
    static final Function<ClientRequest, Boolean> methodEquals(final String method) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                clientRequest.method.equalsIgnoreCase(method)
            }
        }
    }

    /**
     * Provides a function that will return true if the request path equals the specified path.
     *
     * @param path the expected request path
     * @return true if the paths match
     */
    static final Function<ClientRequest, Boolean> pathEquals(final String path) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                clientRequest.path == path
            }
        }
    }

    /**
     * Provides a function that will return true if the request query string parameters match those provided. All of the expected parameter must be
     * present and they must be the only parameters present for the match to be accepted.
     *
     * @param queries the query string parameters as a map
     * @return true if the queries match (fully)
     */
    static final Function<ClientRequest, Boolean> queriesEquals(final Map<String, List<String>> queries) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                queries.every { k, v ->
                    clientRequest.queryParams.containsKey(k) && clientRequest.queryParams.get(k).containsAll(v)
                } && clientRequest.queryParams.every { k, v ->
                    queries.containsKey(k) && queries.get(k).containsAll(v)
                }
            }
        }
    }

    /**
     * Provides a function that will return true if the request headers contain the provided headers and values. This map of headers is not inclusive,
     * but simply a check that the specified headers are present.
     *
     * @param headers the headers that must be in the request for a match
     * @return true if the specified headers are present (non-inclusive)
     */
    static final Function<ClientRequest, Boolean> headersContains(final Map<String, String> headers) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                headers.every { k, v -> v == clientRequest.headers.getFirst(k) }
            }
        }
    }

    /**
     * Provides a function that will return true if the request cookies contain the provided cookies and values. This map of cookies is not inclusive,
     * but simply a check that the specified cookies are present.
     *
     * @param cookies the cookies that must be in the request for a match
     * @return true if the specified cookies are present (non-inclusive)
     */
    static final Function<ClientRequest, Boolean> cookiesContains(final Map<String, String> cookies) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                cookies.every { k, v -> clientRequest.cookies.containsKey(k) && v == clientRequest.cookies.get(k).value }
            }
        }
    }

    /**
     * Provides a function that will return true if the request body content matches the client request body content converted using the specified
     * converter function.
     *
     * @param body the expected body content (converted form)
     * @param converter the function used to convert the client request content to the desired format
     * @return true if the body contents match
     */
    static final Function<ClientRequest, Boolean> bodyEquals(final Object body, final Function<byte[], Object> converter) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                clientRequest.getBody(converter) == body
            }
        }
    }
}
