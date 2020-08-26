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

import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.junit.ErsatzServerExtension
import com.stehno.ersatz.util.HttpClient
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.encdec.Decoders.utf8String
import static okhttp3.MediaType.get
import static okhttp3.RequestBody.create
import static org.junit.jupiter.api.Assertions.assertEquals

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
    private ErsatzServer server
    private HttpClient client

    @BeforeEach void beforeEach() {
        client = new HttpClient()
    }

    @Test @DisplayName('Posting One')
    void postingOne() {
        server.expectations {
            POST('/posting') {
                body INPUT_CONTENT, TEXT_PLAIN
                decoder TEXT_PLAIN, utf8String
                responder {
                    body OUTPUT_CONTENT, TEXT_PLAIN
                }
            }
        }

        Response response = client.post(server.httpUrl('/posting'), okhttp3.RequestBody.create(okhttp3.MediaType.get('text/plain; charset=utf-8'), INPUT_CONTENT))

        assertEquals OUTPUT_CONTENT, response.body().string()
        assertEquals 1, server.expects().requests.size() as int
    }

    @Test @DisplayName('Posting Two')
    void postingTwo() {
        String inputContent = INPUT_CONTENT
        String outputContent = OUTPUT_CONTENT

        server.expectations {
            POST('/posting') {
                body inputContent, TEXT_PLAIN
                decoder TEXT_PLAIN, utf8String
                responder {
                    body outputContent, TEXT_PLAIN
                }
            }
        }

        Response response = client.post(server.httpUrl('/posting'), okhttp3.RequestBody.create(okhttp3.MediaType.get('text/plain; charset=utf-8'), INPUT_CONTENT))

        assertEquals OUTPUT_CONTENT, response.body().string()
        assertEquals 1, server.expects().requests.size() as int
    }

    @Test @DisplayName('Posting Three')
    void postingThree() {
        server.expectations {
            POST('/posting').body(INPUT_CONTENT, TEXT_PLAIN).decoder(TEXT_PLAIN, utf8String)
                .responds().body(OUTPUT_CONTENT, TEXT_PLAIN)
        }

        Response response = client.post(server.httpUrl('/posting'), okhttp3.RequestBody.create(okhttp3.MediaType.get('text/plain; charset=utf-8'), INPUT_CONTENT))

        assertEquals OUTPUT_CONTENT, response.body().string()
        assertEquals 1, server.expects().requests.size() as int
    }
}
