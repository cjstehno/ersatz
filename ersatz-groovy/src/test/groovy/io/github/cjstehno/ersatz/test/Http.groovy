/*
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
package io.github.cjstehno.ersatz.test

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler

import static java.net.http.HttpRequest.BodyPublishers.noBody
import static java.net.http.HttpRequest.newBuilder
import static java.net.http.HttpResponse.BodyHandlers.ofByteArray
import static java.net.http.HttpResponse.BodyHandlers.ofString

class Http {

    private final HttpClient http
    private final String urlBase

    Http(final String urlBase) {
        this.urlBase = urlBase
        http = HttpClient.newHttpClient()
    }

    def <T> HttpResponse<T> get(
        final Map<String, String> headers = [:], final String path, final BodyHandler<T> responseHandler = ofString()
    ) {
        def request = newBuilder().GET().uri(new URI(urlBase + path))
        http.send(applyHeaders(request, headers).build(), responseHandler) as HttpResponse<T>
    }

    def <T> HttpResponse<T> head(
        final Map<String, String> headers = [:], final String path, final BodyHandler<T> responseHandler = ofString()
    ) {
        def request = newBuilder().method('HEAD', noBody()).uri(new URI(urlBase + path))
        http.send(applyHeaders(request, headers).build(), responseHandler) as HttpResponse<T>
    }

    def <T> HttpResponse<T> options(final Map<String, String> headers = [:], final String path) {
        def request = newBuilder().method('OPTIONS', noBody()).uri(new URI(urlBase + path))
        http.send(applyHeaders(request, headers).build(), ofByteArray()) as HttpResponse<T>
    }

    def <T> HttpResponse<T> delete(final Map<String, String> headers = [:], final String path, final BodyHandler<T> responseHandler = ofString()) {
        def request = newBuilder().DELETE().uri(new URI(urlBase + path))
        http.send(applyHeaders(request, headers).build(), responseHandler) as HttpResponse<T>
    }

    def <T> HttpResponse<T> post(final Map<String, String> headers = [:], final String path, final HttpRequest.BodyPublisher requestBody) {
        def request = newBuilder().POST(requestBody).uri(new URI(urlBase + path))
        http.send(applyHeaders(request, headers).build(), ofString()) as HttpResponse<T>
    }

    def <T> HttpResponse<T> put(final Map<String, String> headers = [:], final String path, final HttpRequest.BodyPublisher requestBody) {
        def request = newBuilder().PUT(requestBody).uri(new URI(urlBase + path))
        http.send(applyHeaders(request, headers).build(), ofString()) as HttpResponse<T>
    }

    def <T> HttpResponse<T> patch(final Map<String, String> headers = [:], final String path, final HttpRequest.BodyPublisher requestBody) {
        def request = newBuilder().method('PATCH', requestBody).uri(new URI(urlBase + path))
        http.send(applyHeaders(request, headers).build(), ofString()) as HttpResponse<T>
    }

    private static HttpRequest.Builder applyHeaders(final HttpRequest.Builder request, final Map<String, String> headers) {
        headers?.forEach { n, v ->
            request.header(n, v)
        }
        request
    }
}
