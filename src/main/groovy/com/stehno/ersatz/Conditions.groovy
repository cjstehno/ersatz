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

@CompileStatic
class Conditions {

    /*
* Headers and Cookies are matched with a "contains" strategy, meaning that as long as the expected items are in the request, the match is accepted,
 * while the query strings, method name, path, and body are matched for equivalence.
     */

    static final Function<ClientRequest, Boolean> methodEquals(final String method) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                clientRequest.method.equalsIgnoreCase(method)
            }
        }
    }

    static final Function<ClientRequest, Boolean> pathEquals(final String path) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                clientRequest.path == path
            }
        }
    }

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

    static final Function<ClientRequest, Boolean> headersContains(final Map<String, String> headers) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                headers.every { k, v -> v == clientRequest.headers.getFirst(k) }
            }
        }
    }

    static final Function<ClientRequest, Boolean> cookiesContains(final Map<String, String> cookies) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                cookies.every { k, v -> clientRequest.cookies.containsKey(k) && v == clientRequest.cookies.get(k).value }
            }
        }
    }

    static final Function<ClientRequest, Boolean> bodyEquals(final Object body, final Function<byte[], Object> converter) {
        return new Function<ClientRequest, Boolean>() {
            @Override Boolean apply(final ClientRequest clientRequest) {
                clientRequest.getBody(converter) == body
            }
        }
    }
}
