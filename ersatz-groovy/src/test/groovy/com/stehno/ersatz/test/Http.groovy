/*
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.test

import com.stehno.ersatz.ErsatzServer

import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler

import static java.net.http.HttpRequest.newBuilder
import static java.net.http.HttpResponse.BodyHandlers.ofString

class Http {

    // FIXME: implement simple helper

    private final HttpClient client = HttpClient.newHttpClient()
    private final ErsatzServer server

    Http(final ErsatzServer server) {
        this.server = server
    }

    public <T> HttpResponse<T> GET(final Map<String, String> headers, final String path, final BodyHandler<T> bodyHandler = ofString()) {
        def request = newBuilder().GET().uri(new URI(server.httpUrl(path)))

        headers?.forEach { n, v ->
            request.header(n, v)
        }

        client.send(request.build(), bodyHandler)
    }
}
