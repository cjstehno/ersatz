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
import com.stehno.ersatz.cfg.Request
import com.stehno.ersatz.cfg.RequestWithContent
import com.stehno.ersatz.encdec.RequestDecoders
import com.stehno.ersatz.encdec.ResponseEncoders
import com.stehno.ersatz.server.ClientRequest
import com.stehno.ersatz.server.MockClientRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

import java.util.function.Consumer
import java.util.stream.Stream

import static com.stehno.ersatz.cfg.HttpMethod.*
import static org.hamcrest.Matchers.equalTo
import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.params.provider.Arguments.arguments

class ExpectationsImplTest {

    private static final String PATH = '/somewhere'
    private RequestDecoders decoders
    private ResponseEncoders encoders
    private ExpectationsImpl expectations

    @BeforeEach void beforeEach() {
        decoders = new RequestDecoders()
        encoders = new ResponseEncoders()
        expectations = new ExpectationsImpl(decoders, encoders)
    }

    @ParameterizedTest @DisplayName('method(String)') @MethodSource('methodStringProvider')
    void methodString(final String code, final HttpMethod method) {
        Request request = expectations."$code"(PATH)

        assertTrue request instanceof ErsatzRequest
        assertEquals 1, expectations.requests.size()
        assertTrue expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH))
    }

    private static Stream<Arguments> methodStringProvider() {
        Stream.of(
            arguments('ANY', GET),
            arguments('ANY', HEAD),
            arguments('ANY', DELETE),
            arguments('GET', GET),
            arguments('HEAD', HEAD),
            arguments('DELETE', DELETE),
            arguments('OPTIONS', OPTIONS)
        )
    }

    @ParameterizedTest @DisplayName('method(String,Closure)') @MethodSource('methodStringProvider')
    void methodStringClosure(final String code, final HttpMethod method) {
        Request request = expectations."$code"(PATH, { query('a', 'b') })

        assertTrue request instanceof ErsatzRequest
        assertEquals 1, expectations.requests.size()
        assertTrue expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))
    }

    @ParameterizedTest @DisplayName('method(String,Consumer)') @MethodSource('methodStringProvider')
    void methodStringConsumer(final String code, final HttpMethod method) {
        Request request = expectations."$code"(PATH, { req -> req.query('a', 'b') } as Consumer<Request>)

        assertTrue request instanceof ErsatzRequest
        assertEquals 1, expectations.requests.size()
        assertTrue expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))
    }

    @ParameterizedTest @DisplayName('method(String) with content') @MethodSource('methodStringWithContentProvider')
    void methodStringWithContent(final String code, final HttpMethod method) {
        Request request = expectations."$code"(PATH)

        assertTrue request instanceof ErsatzRequestWithContent
        assertEquals 1, expectations.requests.size()
        assertTrue expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH))
    }

    private static Stream<Arguments> methodStringWithContentProvider() {
        Stream.of(
            arguments('ANY', POST),
            arguments('ANY', PUT),
            arguments('ANY', PATCH),
            arguments('POST', POST),
            arguments('PUT', PUT),
            arguments('PATCH', PATCH)
        )
    }

    @ParameterizedTest @DisplayName('method(String,Closure) with content')
    @MethodSource('methodStringWithContentProvider')
    void methodStringClosureWithContent(final String code, final HttpMethod method) {
        Request request = expectations."$code"(PATH, { query('a', 'b') })

        assertTrue request instanceof ErsatzRequestWithContent
        assertEquals 1, expectations.requests.size()
        assertTrue expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))
    }

    @ParameterizedTest @DisplayName('method(String,Consumer) with content')
    @MethodSource('methodStringWithContentProvider')
    void methodStringConsumerWithContent(final String code, final HttpMethod method) {
        Request request = expectations."$code"(PATH, { req -> req.query('a', 'b') } as Consumer<RequestWithContent>)

        assertTrue request instanceof ErsatzRequestWithContent
        assertEquals 1, expectations.requests.size()
        assertTrue expectations.requests[0].matches(new MockClientRequest(method: method, path: PATH).query('a', 'b'))
    }

    @ParameterizedTest @DisplayName('matching') @MethodSource('matchingProvider')
    void matching(final HttpMethod method, final String path) {
        expectations.ANY('/charlie')
        expectations.POST('/alpha')
        expectations.POST('/bravo')
        expectations.DELETE('/alpha')
        expectations.GET('/alpha')

        ClientRequest cr = new MockClientRequest(method: method, path: path)

        Request req = expectations.findMatch(cr).get()

        assertTrue req.matches(cr)
    }

    private static Stream<Arguments> matchingProvider() {
        Stream.of(
            arguments(GET, '/charlie'),
            arguments(POST, '/charlie'),
            arguments(PUT, '/charlie'),
            arguments(PATCH, '/charlie'),
            arguments(DELETE, '/charlie'),
            arguments(GET, '/alpha'),
            arguments(POST, '/alpha'),
            arguments(POST, '/bravo'),
            arguments(DELETE, '/alpha')
        )
    }

    @Test @DisplayName('verification (success)')
    void verificationSuccess() {
        RequestWithContent req = expectations.POST('/alpha').called(equalTo(1))
        ((ErsatzRequestWithContent) req).mark(new MockClientRequest())

        assertTrue expectations.verify()
    }

    @Test @DisplayName('verification (failure)')
    void verificationFailure() {
        expectations.POST('/alpha').called(equalTo(1))

        def thrown = assertThrows(IllegalArgumentException, {
            expectations.verify()
        })

        assertEquals 'Expectations for Expectations (ErsatzRequestWithContent): <POST>, "/alpha",  were not met.', thrown.message
    }

    @ParameterizedTest @DisplayName('wildcard path')
    @CsvSource([
        '/alpha',
        '/bravo',
        '/charlie/delta'
    ])
    void wildcardPath(final String path) {
        expectations.GET('*')

        ClientRequest cr = new MockClientRequest(method: GET, path: path)

        assertTrue expectations.findMatch(cr).get().matches(cr)
    }

    @ParameterizedTest @DisplayName('matching wildcarded any method') @MethodSource('methodPathProvider')
    void matchingWildcardedAny(final HttpMethod method, final String path) {
        expectations.ANY('*')

        ClientRequest cr = new MockClientRequest(method: method, path: path)

        assertTrue expectations.findMatch(cr).get().matches(cr)
    }

    private static Stream<Arguments> methodPathProvider() {
        Stream.of(
            arguments(GET, '/alpha'),
            arguments(POST, '/bravo'),
            arguments(PUT, '/charlie'),
            arguments(PATCH, '/delta'),
            arguments(DELETE, '/echo'),
            arguments(OPTIONS, '/foxtrot')
        )
    }
}
