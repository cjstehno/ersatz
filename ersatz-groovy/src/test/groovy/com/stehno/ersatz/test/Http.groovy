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
