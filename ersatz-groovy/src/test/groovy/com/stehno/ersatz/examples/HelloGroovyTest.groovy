package com.stehno.ersatz.examples


import com.stehno.ersatz.GroovyErsatzServer
import com.stehno.ersatz.junit.ErsatzServerExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import java.net.http.HttpRequest

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static java.net.http.HttpClient.newHttpClient
import static java.net.http.HttpResponse.BodyHandlers.ofString
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(ErsatzServerExtension)
class HelloGroovyTest {

    private GroovyErsatzServer server

    @Test void 'say hello'() {
        server.expectations {
            GET('/say/hello') {
                called 1
                query 'name', 'Ersatz'
                responder {
                    body 'Hello, Ersatz', TEXT_PLAIN
                }
            }
        }

        final var request = HttpRequest
                .newBuilder(new URI(server.httpUrl('/say/hello?name=Ersatz')))
                .GET()
                .build()

        final var response = newHttpClient().send(request, ofString())

        assertEquals 200, response.statusCode()
        assertEquals 'Hello, Ersatz', response.body()
        assertTrue server.verify()
    }
}
