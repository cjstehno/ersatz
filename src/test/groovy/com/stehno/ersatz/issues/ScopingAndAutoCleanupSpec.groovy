/*
 * Copyright (C) 2018 Christopher J. Stehno
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
import com.stehno.ersatz.util.HttpClient
import okhttp3.Response
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.Decoders.utf8String
import static okhttp3.MediaType.get
import static okhttp3.RequestBody.create

/**
 * Tests related to a couple reported issues:
 *
 * https://github.com/cjstehno/ersatz/issues/107
 * https://github.com/cjstehno/ersatz/issues/108
 * https://github.com/cjstehno/ersatz/issues/110
 */
class ScopingAndAutoCleanupSpec extends Specification {

    private static final String INPUT_CONTENT = 'input'
    private static final String OUTPUT_CONTENT = 'output'
    @AutoCleanup('stop') private final ErsatzServer server = new ErsatzServer()
    private final HttpClient client = new HttpClient()

    void 'Posting One'() {
        setup:
        server.expectations {
            post('/posting') {
                body INPUT_CONTENT, TEXT_PLAIN
                decoder TEXT_PLAIN, utf8String
                responder {
                    body OUTPUT_CONTENT, TEXT_PLAIN
                }
            }
        }

        when:
        Response response = client.post(server.httpUrl('/posting'), create(get('text/plain; charset=utf-8'), INPUT_CONTENT))

        then:
        response.body().string() == OUTPUT_CONTENT

        and:
        server.expectations.requests.size() == 1
    }

    void 'Posting Two'() {
        setup:
        String inputContent = INPUT_CONTENT
        String outputContent = OUTPUT_CONTENT

        server.expectations {
            post('/posting') {
                body inputContent, TEXT_PLAIN
                decoder TEXT_PLAIN, utf8String
                responder {
                    body outputContent, TEXT_PLAIN
                }
            }
        }

        when:
        Response response = client.post(server.httpUrl('/posting'), create(get('text/plain; charset=utf-8'), INPUT_CONTENT))

        then:
        response.body().string() == OUTPUT_CONTENT

        and:
        server.expectations.requests.size() == 1
    }

    void 'Posting Three'() {
        setup:
        server.expectations {
            post('/posting').body(INPUT_CONTENT, TEXT_PLAIN).decoder(TEXT_PLAIN, utf8String)
                .responds().body(OUTPUT_CONTENT, TEXT_PLAIN)
        }

        when:
        Response response = client.post(server.httpUrl('/posting'), create(get('text/plain; charset=utf-8'), INPUT_CONTENT))

        then:
        response.body().string() == OUTPUT_CONTENT

        and:
        server.expectations.requests.size() == 1
    }

    /**
     * Example how any missing property is converted into `get(name)` expectation.
     */
    @Issue('https://github.com/cjstehno/ersatz/issues/110')
    void 'Posting Four'() {
        setup:
            server.expectations {
                post(('/' + itsAKindOfMagic).trim()) {
                    body INPUT_CONTENT, TEXT_PLAIN
                    decoder TEXT_PLAIN, utf8String
                    responder {
                        body OUTPUT_CONTENT, TEXT_PLAIN
                    }
                }
            }

        when:
            Response response = client.post(server.httpUrl('/Expectations (ErsatzRequest): <GET>, "itsAKindOfMagic",'), create(get('text/plain; charset=utf-8'), INPUT_CONTENT))

        then:
            response.body().string() == OUTPUT_CONTENT

        and:
            server.expectations.requests.size() == 2
    }
}
