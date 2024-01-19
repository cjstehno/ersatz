/*
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
package io.github.cjstehno.ersatz.examples

import io.github.cjstehno.ersatz.GroovyErsatzServer
import io.github.cjstehno.ersatz.cfg.HttpMethod
import io.github.cjstehno.ersatz.cfg.ServerConfig
import io.github.cjstehno.ersatz.junit.ApplyServerConfig
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching
import static java.net.http.HttpClient.newHttpClient
import static java.net.http.HttpRequest.newBuilder
import static java.net.http.HttpResponse.BodyHandlers.ofString
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(ErsatzServerExtension)
class HelloGroovyTest {

    private static final String KEY_HEADER = 'key'
    private GroovyErsatzServer server

    @Test
    void 'say hello'() {
        server.expectations {
            GET('/say/hello') {
                called 1
                query 'name', 'Ersatz'
                responder {
                    body 'Hello, Ersatz', TEXT_PLAIN
                }
            }
        }

        def request = newBuilder(new URI(server.httpUrl('/say/hello?name=Ersatz'))).GET().build()
        def response = newHttpClient().send(request, ofString())

        assertEquals 200, response.statusCode()
        assertEquals 'Hello, Ersatz', response.body()
        assertTrue server.verify()
    }

    @Test
    @ApplyServerConfig('localConfig')
    void 'Using annotation config with field and param'(final GroovyErsatzServer myServer) {
        myServer.expectations {
            GET('/foo') {
                called 1
                responder {
                    code 200
                }
            }
        }

        assertEquals(
            200,
            newHttpClient().send(
                newBuilder(URI.create(myServer.httpUrl('/foo'))).GET().header('key', 'unlocked').build(),
                ofString()
            ).statusCode()
        )
        assertEquals(
            404,
            newHttpClient().send(
                newBuilder(URI.create(myServer.httpUrl('/foo'))).GET().build(),
                ofString()
            ).statusCode()
        )
        assertTrue(myServer.verify())
    }

    @SuppressWarnings(['unused', 'UnusedPrivateMethod'])
    private static void localConfig(final ServerConfig config) {
        config.requirements {
            that HttpMethod.GET, pathMatching('/foo'), { cfg ->
                cfg.header(KEY_HEADER, 'unlocked')
            }
        }
    }
}
