/*
 * Copyright (C) 2016 Christopher J. Stehno
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

import com.stehno.ersatz.Request
import com.stehno.ersatz.RequestWithContent
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Consumer

class ExpectationsImplSpec extends Specification {

    private static final String PATH = '/somewhere'
    private final ExpectationsImpl expectations = new ExpectationsImpl()

    @Unroll def '#method(String)'() {
        when:
        Request request = expectations."${method.toLowerCase()}"(PATH)

        then:
        request instanceof ErsatzRequest
        assertExpectedRequest method

        where:
        method << ['GET', 'HEAD', 'DELETE']
    }

    @Unroll def '#method(String,Closure)'() {
        when:
        Request request = expectations."${method.toLowerCase()}"(PATH, { query('a', 'b') })

        then:
        request instanceof ErsatzRequest
        assertExpectedRequest method
        expectations.requests[0].getQuery('a') == ['b']

        where:
        method << ['GET', 'HEAD', 'DELETE']
    }

    @Unroll def '#method(String,Consumer)'() {
        when:
        Request request = expectations."${method.toLowerCase()}"(PATH, { req -> req.query('a', 'b') } as Consumer<Request>)

        then:
        request instanceof ErsatzRequest
        assertExpectedRequest method
        expectations.requests[0].getQuery('a') == ['b']

        where:
        method << ['GET', 'HEAD', 'DELETE']
    }

    @Unroll def '#method(String) (with content)'() {
        when:
        Request request = expectations."${method.toLowerCase()}"(PATH)

        then:
        request instanceof ErsatzRequestWithContent
        assertExpectedRequest method

        where:
        method << ['POST', 'PUT', 'PATCH']
    }

    @Unroll def '#method(String,Closure) (with content)'() {
        when:
        Request request = expectations."${method.toLowerCase()}"(PATH, { query('a', 'b') })

        then:
        request instanceof ErsatzRequestWithContent
        assertExpectedRequest method
        expectations.requests[0].getQuery('a') == ['b']

        where:
        method << ['POST', 'PUT', 'PATCH']
    }

    @Unroll def '#method(String,Consumer) (with content)'() {
        when:
        Request request = expectations."${method.toLowerCase()}"(PATH, { req -> req.query('a', 'b') } as Consumer<RequestWithContent>)

        then:
        request instanceof ErsatzRequestWithContent
        assertExpectedRequest method
        expectations.requests[0].getQuery('a') == ['b']

        where:
        method << ['POST', 'PUT', 'PATCH']
    }

    private boolean assertExpectedRequest(final String method) {
        assert expectations.requests.size() == 1
        assert expectations.requests[0].method == method
        assert expectations.requests[0].path == PATH
        true
    }
}
