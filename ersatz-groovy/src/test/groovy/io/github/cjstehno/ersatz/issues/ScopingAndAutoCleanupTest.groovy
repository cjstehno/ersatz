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
package io.github.cjstehno.ersatz.issues

import io.github.cjstehno.ersatz.GroovyErsatzServer
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension
import io.github.cjstehno.ersatz.test.Http
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import java.net.http.HttpRequest

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static io.github.cjstehno.ersatz.encdec.Decoders.utf8String
import static java.nio.charset.StandardCharsets.UTF_8
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests related to a couple reported issues:
 *
 * https://github.com/cjstehno/ersatz/issues/107
 * https://github.com/cjstehno/ersatz/issues/108
 * https://github.com/cjstehno/ersatz/issues/110
 */
@ExtendWith(ErsatzServerExtension)
class ScopingAndAutoCleanupTest {

    private static final String INPUT_CONTENT = 'input'
    private static final String OUTPUT_CONTENT = 'output'
    private Http http

    @BeforeEach
    void beforeEach(final GroovyErsatzServer server) {
        // this is a bit of a hack do to the order of operations
        server.start()

        http = new Http(server.httpUrl)
    }

    @Test
    @DisplayName('Posting One')
    void postingOne(final GroovyErsatzServer server) {
        server.expectations {
            POST('/posting') {
                called 1
                body INPUT_CONTENT, TEXT_PLAIN
                decoder TEXT_PLAIN, utf8String
                responder {
                    body OUTPUT_CONTENT, TEXT_PLAIN
                }
            }
        }

        def response = http.post('Content-Type': TEXT_PLAIN.value, '/posting', HttpRequest.BodyPublishers.ofString(INPUT_CONTENT, UTF_8))

        assertEquals OUTPUT_CONTENT, response.body()
        assertTrue server.verify()
    }

    @Test
    @DisplayName('Posting Two')
    void postingTwo(final GroovyErsatzServer server) {
        String inputContent = INPUT_CONTENT
        String outputContent = OUTPUT_CONTENT

        server.expectations {
            POST('/posting') {
                called 1
                body inputContent, TEXT_PLAIN
                decoder TEXT_PLAIN, utf8String
                responder {
                    body outputContent, TEXT_PLAIN
                }
            }
        }

        def response = http.post('Content-Type': TEXT_PLAIN.value, '/posting', HttpRequest.BodyPublishers.ofString(INPUT_CONTENT, UTF_8))

        assertEquals OUTPUT_CONTENT, response.body()
        assertTrue server.verify()
    }

    @Test
    @DisplayName('Posting Three')
    void postingThree(final GroovyErsatzServer server) {
        server.expectations {
            POST('/posting').body(INPUT_CONTENT, TEXT_PLAIN).decoder(TEXT_PLAIN, utf8String).called(1)
                    .responds().body(OUTPUT_CONTENT, TEXT_PLAIN)
        }

        def response = http.post('Content-Type': TEXT_PLAIN.value, '/posting', HttpRequest.BodyPublishers.ofString(INPUT_CONTENT, UTF_8))

        assertEquals OUTPUT_CONTENT, response.body()
        assertTrue server.verify()
    }
}
