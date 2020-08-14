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
package com.stehno.ersatz.impl

import com.stehno.ersatz.cfg.HttpMethod
import com.stehno.ersatz.encdec.Cookie
import com.stehno.ersatz.encdec.ResponseEncoders
import com.stehno.ersatz.server.MockClientRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.stream.Stream

import static com.stehno.ersatz.cfg.HttpMethod.POST
import static com.stehno.ersatz.cfg.HttpMethod.PUT
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith
import static org.junit.jupiter.params.provider.Arguments.arguments

class UnmatchedRequestReportTest {

    private static final String RED = '\u001b[31m'
    private static final String GREEN = '\u001b[32m'
    private static final String RESET = '\u001b[0m'
    private static final String BODY = 'This is some text content'

    @ParameterizedTest @DisplayName('unmatched report with type #contentType should print #content')
    @MethodSource('contentProvider')
    void unmatched(String contentType, String content) {
        def headers = new LinkedHashMap<String, Deque<String>>()

        Deque<String> alphas = new ArrayDeque<>()
        alphas.add('bravo-1')
        alphas.add('bravo-2')
        headers.put('alpha', alphas)

        Deque<String> charlies = new ArrayDeque<>()
        charlies.add('delta')
        headers.put('charlie', charlies)

        MockClientRequest request = new MockClientRequest(
            method: HttpMethod.GET,
            protocol: 'HTTP',
            path: '/alpha/foo',
            headers: headers,
            contentLength: 1234,
            contentType: contentType,
            characterEncoding: 'UTF-8',
            body: BODY.bytes,
            cookies: [ident: new Cookie('asdfasdfasdf', null, null, null, 0, false, 0, false)]
        )
        request.query('selected', 'one', 'two')
        request.query('id', '1002')

        List<ErsatzRequest> expectations = [
            new ErsatzRequest(POST, equalTo('/alpha/foo'), new ResponseEncoders()),
            new ErsatzRequest(PUT, startsWith('/alpha/bar'), new ResponseEncoders()).protocol('HTTPS')
        ]

        when:
        String string = new UnmatchedRequestReport(request, expectations).render()

        then:
        string == """            # Unmatched Request
            
            HTTP GET /alpha/foo ? selected=[one, two], id=[1002]
            Headers:
             - alpha: [bravo-1, bravo-2]
             - charlie: [delta]
             - Content-Type: [$contentType]
            Cookies:
             - ident (null, null): asdfasdfasdf
            Character-Encoding: UTF-8
            Content-type: $contentType
            Content-Length: 1234
            Content:
              $content
            
            # Expectations
            
            Expectation 0 (2 matchers):
              ${RED}X HTTP method matches <POST>${RESET}
              ${GREEN}✓${RESET} Path matches "/alpha/foo"
              (2 matchers: 1 matched, ${RED}1 failed${RESET})
            
            Expectation 1 (3 matchers):
              ${RED}X HTTP method matches <PUT>${RESET}
              ${RED}X Path matches a string starting with "/alpha/bar"${RESET}
              ${RED}X Protocol matches a string equal to "HTTPS" ignoring case${RESET}
              (3 matchers: 0 matched, ${RED}3 failed${RESET})
              
        """.stripIndent()
    }

    private static Stream<Arguments> contentProvider() {
        Stream.of(
            arguments('application/octet-stream', '[84, 104, 105, 115, 32, 105, 115, 32, 115, 111, 109, 101, 32, 116, 101, 120, 116, 32, 99, 111, 110, 116, 101, 110, 116]'),
            arguments('text/plain', BODY),
            arguments('text/csv', BODY),
            arguments('application/json', BODY),
            arguments('application/x-www-form-urlencoded', BODY)
        )
    }
}