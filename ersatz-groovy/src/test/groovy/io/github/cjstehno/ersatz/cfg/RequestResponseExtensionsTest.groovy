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
package io.github.cjstehno.ersatz.cfg

import io.github.cjstehno.ersatz.GroovyErsatzServer
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension
import io.github.cjstehno.ersatz.test.Http
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.stream.Stream

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static io.github.cjstehno.ersatz.encdec.Decoders.utf8String
import static java.net.http.HttpRequest.BodyPublishers.ofString
import static java.nio.charset.StandardCharsets.UTF_8
import static org.hamcrest.Matchers.startsWith
import static org.junit.jupiter.api.Assertions.assertArrayEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(ErsatzServerExtension)
class RequestResponseExtensionsTest {

    private static final String TEXT_RESPONSE = 'I am some test to respond with'
    private final GroovyErsatzServer server = new GroovyErsatzServer({
        reportToConsole()
        decoder TEXT_PLAIN, utf8String
    })
    private Http http

    @BeforeEach
    void before() {
        http = new Http(server.httpUrl)
    }

    @Test
    void anyExtensions() {
        server.expectations {
            ANY('/whatever') {
                called 1
                responder {
                    body TEXT_RESPONSE, TEXT_PLAIN
                    chunked {
                        chunks 2
                        delay 1
                    }
                }
            }
        }

        def response = http.get('/whatever')
        assertEquals TEXT_RESPONSE, response.body()
        assertTrue server.verify()
    }

    @Test
    void getExtension() {
        server.expectations {
            GET(startsWith('/some/')) {
                called 1
                responder {
                    body TEXT_RESPONSE, TEXT_PLAIN
                }
            }
        }

        def response = http.get('/some/thing')
        assertEquals TEXT_RESPONSE, response.body()
        assertTrue server.verify()
    }

    @Test
    void headExtensions() {
        server.expectations {
            HEAD(startsWith('/some/')) {
                called 1
                responder {
                    code 200
                }
            }
            HEAD('/other') {
                called 1
                responder {
                    code 200
                }
            }
        }

        assertEquals 200, http.head('/some/thing').statusCode()
        assertEquals 200, http.head('/other').statusCode()
        assertTrue server.verify()
    }

    @Test
    void deleteExtensions() {
        server.expectations {
            DELETE(startsWith('/some/')) {
                called 1
                responder {
                    code 200
                }
            }
            DELETE('/other') {
                called 1
                responder {
                    code 200
                }
            }
        }

        assertEquals 200, http.delete('/some/thing').statusCode()
        assertEquals 200, http.delete('/other').statusCode()
        assertTrue server.verify()
    }

    @Test
    void postExtensions() {
        server.expectations {
            POST(startsWith('/some/')) {
                called 1
                body 'Take this', TEXT_PLAIN
                responder {
                    code 200
                    body TEXT_RESPONSE, TEXT_PLAIN
                }
            }
            POST('/other') {
                called 1
                body 'Take that', TEXT_PLAIN
                responder {
                    code 200
                    body TEXT_RESPONSE, TEXT_PLAIN
                }
            }
        }

        assertEquals TEXT_RESPONSE, http.post('Content-Type': TEXT_PLAIN.value, '/some/thing', ofString('Take this', UTF_8)).body()
        assertEquals TEXT_RESPONSE, http.post('Content-Type': TEXT_PLAIN.value, '/other', ofString('Take that', UTF_8)).body()
        assertTrue server.verify()
    }

    @Test
    void putExtensions() {
        server.expectations {
            PUT(startsWith('/some/')) {
                called 1
                body 'Take this', TEXT_PLAIN
                responder {
                    code 200
                }
            }
            PUT('/other') {
                called 1
                body 'Take that', TEXT_PLAIN
                responder {
                    code 200
                }
            }
        }

        assertEquals 200, http.put('Content-Type': TEXT_PLAIN.value, '/some/thing', ofString('Take this', UTF_8)).statusCode()
        assertEquals 200, http.put('Content-Type': TEXT_PLAIN.value, '/other', ofString('Take that', UTF_8)).statusCode()
        assertTrue server.verify()
    }

    @Test
    void patchExtensions() {
        server.expectations {
            PATCH(startsWith('/some/')) {
                called 1
                body 'Take this', TEXT_PLAIN
                responder {
                    code 200
                }
            }
            PATCH('/other') {
                called 1
                body 'Take that', TEXT_PLAIN
                responder {
                    code 200
                }
            }
        }

        assertEquals 200, http.patch('Content-Type': TEXT_PLAIN.value, '/some/thing', ofString('Take this', UTF_8)).statusCode()
        assertEquals 200, http.patch('Content-Type': TEXT_PLAIN.value, '/other', ofString('Take that', UTF_8)).statusCode()
        assertTrue server.verify()
    }

    @ParameterizedTest(name = '[{index}] allowed options: https({0}) {1} -> {2}')
    @MethodSource('optionsProvider')
    void optionsExtensions(final String path, final Collection<String> allowed) {
        server.expectations {
            OPTIONS(startsWith('/options')) {
                responder {
                    allows HttpMethod.GET, HttpMethod.POST
                    code 200
                }
            }
            OPTIONS('/*') {
                responder {
                    allows HttpMethod.DELETE, HttpMethod.GET, HttpMethod.OPTIONS
                    code 200
                }
            }
        }

        def response = http.options(path)

        assertEquals 200, response.statusCode()
        assertEquals allowed.size(), response.headers().allValues('Allow').size()
        assertTrue response.headers().allValues('Allow').containsAll(allowed)
        assertArrayEquals(new byte[0], response.body() as byte[])
    }

    @SuppressWarnings('UnusedPrivateMethod')
    private static Stream<Arguments> optionsProvider() {
        Stream.of(
            Arguments.arguments('/options', List.of('GET', 'POST')),
            Arguments.arguments('/*', List.of('OPTIONS', 'GET', 'DELETE'))
        )
    }
}