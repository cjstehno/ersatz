/*
 * Copyright (C) 2019 Christopher J. Stehno
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

import com.stehno.ersatz.HttpMethod
import com.stehno.ersatz.ResponseEncoders
import io.undertow.server.handlers.CookieImpl
import io.undertow.util.HeaderMap
import spock.lang.Specification
import spock.lang.Unroll

import static com.stehno.ersatz.HttpMethod.POST
import static com.stehno.ersatz.HttpMethod.PUT
import static io.undertow.util.HttpString.tryFromString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith

class UnmatchedRequestReportSpec extends Specification {

    private static final String RED = '\u001b[31m'
    private static final String GREEN = '\u001b[32m'
    private static final String RESET = '\u001b[0m'
    private static final String BODY = 'This is some text content'

    @Unroll
    void 'unmatched report with type #contentType should print #content'() {
        setup:
        HeaderMap headers = new HeaderMap()
        headers.add(tryFromString('alpha'), 'bravo-1')
        headers.add(tryFromString('alpha'), 'bravo-2')
        headers.add(tryFromString('charlie'), 'delta')

        MockClientRequest request = new MockClientRequest(
            method: HttpMethod.GET,
            protocol: 'HTTP',
            path: '/alpha/foo',
            headers: headers,
            contentLength: 1234,
            contentType: contentType,
            characterEncoding: 'UTF-8',
            body: BODY.bytes,
            cookies: [ident: new CookieImpl('ident', 'asdfasdfasdf')]
        )
        request.query('selected', 'one', 'two')
        request.query('id', '1002')

        List<ErsatzRequest> expectations = [
            new ErsatzRequest(POST, equalTo('/alpha/foo'), new ResponseEncoders()),
            new ErsatzRequest(PUT, startsWith('/alpha/bar'), new ResponseEncoders()).protocol('HTTPS')
        ]

        when:
        String string = new UnmatchedRequestReport(request, expectations).toString()

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
              ${RED}X Protocol matches equalToIgnoringCase("HTTPS")${RESET}
              (3 matchers: 0 matched, ${RED}3 failed${RESET})
              
        """.stripIndent()

        where:
        contentType                         | content
        'application/octet-stream'          | '[84, 104, 105, 115, 32, 105, 115, 32, 115, 111, 109, 101, 32, 116, 101, 120, 116, 32, 99, 111, 110, 116, 101, 110, 116]'
        'text/plain'                        | BODY
        'text/csv'                          | BODY
        'application/json'                  | BODY
        'application/x-www-form-urlencoded' | BODY
    }
}
