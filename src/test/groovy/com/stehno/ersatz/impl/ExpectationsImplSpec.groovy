/*
 * Copyright (C) 2017 Christopher J. Stehno
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

import com.stehno.ersatz.*
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Consumer

import static com.stehno.ersatz.HttpMethod.*
import static org.hamcrest.Matchers.equalTo

class ExpectationsImplSpec extends Specification {

    private static final String PATH = '/somewhere'
    private final RequestDecoders decoders = new RequestDecoders()
    private final ResponseEncoders encoders = new ResponseEncoders()
    private final ExpectationsImpl expectations = new ExpectationsImpl(decoders, encoders)

    @Unroll '#method(String)'() {
        when:
        Request request = expectations."$code"(PATH)

        then:
        request instanceof ErsatzRequest
        expectations.requests.size() == 1
        expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH))

        where:
        code      | method
        'any'     | GET
        'any'     | HEAD
        'any'     | DELETE
        'get'     | GET
        'head'    | HEAD
        'delete'  | DELETE
        'options' | OPTIONS
    }

    @Unroll '#method(String,Closure)'() {
        when:
        Request request = expectations."$code"(PATH, { query('a', 'b') })

        then:
        request instanceof ErsatzRequest
        expectations.requests.size() == 1
        expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))

        where:
        code      | method
        'any'     | GET
        'any'     | HEAD
        'any'     | DELETE
        'get'     | GET
        'head'    | HEAD
        'delete'  | DELETE
        'options' | OPTIONS
    }

    @Unroll '#method(String,Consumer)'() {
        when:
        Request request = expectations."$code"(PATH, { req -> req.query('a', 'b') } as Consumer<Request>)

        then:
        request instanceof ErsatzRequest
        expectations.requests.size() == 1
        expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))

        where:
        code      | method
        'any'     | GET
        'any'     | HEAD
        'any'     | DELETE
        'get'     | GET
        'head'    | HEAD
        'delete'  | DELETE
        'options' | OPTIONS
    }

    @Unroll '#method(String) (with content)'() {
        when:
        Request request = expectations."$code"(PATH)

        then:
        request instanceof ErsatzRequestWithContent
        expectations.requests.size() == 1
        expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH))

        where:
        code    | method
        'any'   | POST
        'any'   | PUT
        'any'   | PATCH
        'post'  | POST
        'put'   | PUT
        'patch' | PATCH
    }

    @Unroll '#method(String,Closure) (with content)'() {
        when:
        Request request = expectations."$code"(PATH, { query('a', 'b') })

        then:
        request instanceof ErsatzRequestWithContent
        expectations.requests.size() == 1
        expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))

        where:
        code    | method
        'any'   | POST
        'any'   | PUT
        'any'   | PATCH
        'post'  | POST
        'put'   | PUT
        'patch' | PATCH
    }

    @Unroll '#method(String,Consumer) (with content)'() {
        when:
        Request request = expectations."$code"(PATH, { req -> req.query('a', 'b') } as Consumer<RequestWithContent>)

        then:
        request instanceof ErsatzRequestWithContent
        expectations.requests.size() == 1
        expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))

        where:
        code    | method
        'any'   | POST
        'any'   | PUT
        'any'   | PATCH
        'post'  | POST
        'put'   | PUT
        'patch' | PATCH
    }

    def 'matching'() {
        setup:
        expectations.any('/charlie')
        expectations.post('/alpha')
        expectations.post('/bravo')
        expectations.delete('/alpha')
        expectations.get('/alpha')

        ClientRequest cr = new MockClientRequest(method: method, path: path)

        when:
        Request req = expectations.findMatch(cr)

        then:
        req.matches(cr)

        where:
        method | path
        GET    | '/charlie'
        POST   | '/charlie'
        PUT    | '/charlie'
        PATCH  | '/charlie'
        DELETE | '/charlie'
        GET    | '/alpha'
        POST   | '/alpha'
        POST   | '/bravo'
        DELETE | '/alpha'
    }

    def 'verification (success)'() {
        setup:
        RequestWithContent req = expectations.post('/alpha').called(equalTo(1))
        ((ErsatzRequestWithContent) req).mark(new MockClientRequest())

        expect:
        expectations.verify()
    }

    def 'verification (failure)'() {
        setup:
        expectations.post('/alpha').called(equalTo(1))

        when:
        expectations.verify()

        then:
        def ae = thrown(AssertionError)
        ae.message == 'Expectations for Expectations (ErsatzRequestWithContent): <POST>, "/alpha",  were not met.. ' +
            'Expression: (com.stehno.ersatz.impl.ErsatzRequest -> com.stehno.ersatz.impl.ErsatzRequest) r.verify()'
    }

    @Unroll 'wildcard path (#path)'() {
        setup:
        expectations.get('*')

        ClientRequest cr = new MockClientRequest(method: GET, path: path)

        expect:
        expectations.findMatch(cr).matches(cr)

        where:
        path << ['/alpha', '/bravo', '/charlie/delta']
    }


    @Unroll 'matching wildcarded any (#method)'() {
        setup:
        expectations.any('*')

        ClientRequest cr = new MockClientRequest(method: method, path: path)

        expect:
        expectations.findMatch(cr).matches(cr)

        where:
        method  | path
        GET     | '/alpha'
        POST    | '/bravo'
        PUT     | '/charlie'
        PATCH   | '/delta'
        DELETE  | '/echo'
        OPTIONS | '/foxtrot'
    }
}
