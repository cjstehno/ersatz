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
package com.stehno.ersatz.issues

import com.stehno.ersatz.GroovyErsatzServer
import com.stehno.ersatz.junit.ErsatzServerExtension
import com.stehno.ersatz.test.Http
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.encdec.Decoders.utf8String
import static java.net.http.HttpRequest.BodyPublishers.ofString
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
    private GroovyErsatzServer server
    private Http http

    @BeforeEach void beforeEach() {
        http = new Http(server)
    }

    @Test @DisplayName('Posting One')
    void postingOne() {
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

        def response = http.POST('Content-Type': TEXT_PLAIN.value, '/posting', ofString(INPUT_CONTENT, UTF_8))

        assertEquals OUTPUT_CONTENT, response.body()
        assertTrue server.verify()
    }

    @Test @DisplayName('Posting Two')
    void postingTwo() {
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

        def response = http.POST('Content-Type': TEXT_PLAIN.value, '/posting', ofString(INPUT_CONTENT, UTF_8))

        assertEquals OUTPUT_CONTENT, response.body()
        assertTrue server.verify()
    }

    @Test @DisplayName('Posting Three')
    void postingThree() {
        server.expectations {
            POST('/posting').body(INPUT_CONTENT, TEXT_PLAIN).decoder(TEXT_PLAIN, utf8String).called(1)
                .responds().body(OUTPUT_CONTENT, TEXT_PLAIN)
        }

        def response = http.POST('Content-Type': TEXT_PLAIN.value, '/posting', ofString(INPUT_CONTENT, UTF_8))

        assertEquals OUTPUT_CONTENT, response.body()
        assertTrue server.verify()
    }
}
