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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.cfg.Request;
import com.stehno.ersatz.cfg.RequestWithContent;
import com.stehno.ersatz.encdec.RequestDecoders;
import com.stehno.ersatz.encdec.ResponseEncoders;
import com.stehno.ersatz.server.ClientRequest;
import com.stehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.stehno.ersatz.cfg.HttpMethod.*;
import static java.util.Arrays.stream;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.commons.util.ReflectionUtils.findMethod;

class ExpectationsImplTest {

    private static final Consumer<Request> QUERY_REQUEST_EXPECTATION = r -> r.query("a", "b");
    private static final Consumer<RequestWithContent> QUERY_REQUEST_WITH_CONTENT_EXPECTATION = r -> r.query("a", "b");
    private static final String PATH = "/somewhere";
    private static final String OTHER_PATH = "/elsewhere";
    private RequestDecoders decoders;
    private ResponseEncoders encoders;
    private ExpectationsImpl expectations;

    @BeforeEach void beforeEach() {
        decoders = new RequestDecoders();
        encoders = new ResponseEncoders();
        expectations = new ExpectationsImpl(encoders, decoders);
    }

    @Test @DisplayName("ANY(String)")
    void anyMethodString() {
        final var request = expectations.ANY(PATH);

        assertRequest(request);

        assertRequestMatch(request(GET), true);
        assertRequestMatch(request(HEAD), true);
        assertRequestMatch(request(DELETE), true);
        assertRequestMatch(request(OPTIONS), true);
        assertRequestMatch(request(POST), true);
        assertRequestMatch(request(PUT), true);
        assertRequestMatch(request(PATCH), true);
    }

    @Test @DisplayName("ANY(Matcher)")
    void anyMethodMatcher() {
        final var request = expectations.ANY(equalTo(PATH));

        assertRequest(request);

        assertRequestMatch(request(GET), true);
        assertRequestMatch(request(GET, OTHER_PATH), false);
        assertRequestMatch(request(HEAD), true);
        assertRequestMatch(request(DELETE), true);
        assertRequestMatch(request(OPTIONS), true);
        assertRequestMatch(request(POST), true);
        assertRequestMatch(request(PUT), true);
        assertRequestMatch(request(PATCH), true);
    }

    @Test @DisplayName("ANY(String,Consumer)")
    void anyMethodStringConsumer() {
        final var request = expectations.ANY(PATH, QUERY_REQUEST_EXPECTATION);

        assertRequest(request);

        assertRequestMatch(request(GET).query("a", "b"), true);
        assertRequestMatch(request(HEAD).query("a", "b"), true);
        assertRequestMatch(request(DELETE).query("a", "b"), true);
        assertRequestMatch(request(OPTIONS).query("a", "b"), true);
        assertRequestMatch(request(POST).query("a", "b"), true);
        assertRequestMatch(request(PUT).query("a", "b"), true);
        assertRequestMatch(request(PATCH).query("a", "b"), true);

        assertRequestMatch(request(GET).query("d", "e"), false);
    }

    @Test @DisplayName("ANY(Matcher,Consumer)")
    void anyMethodMatcherConsumer() {
        final var request = expectations.ANY(equalTo(PATH), QUERY_REQUEST_EXPECTATION);

        assertRequest(request);

        assertRequestMatch(request(GET).query("a", "b"), true);
        assertRequestMatch(request(GET, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), true);
        assertRequestMatch(request(DELETE).query("a", "b"), true);
        assertRequestMatch(request(OPTIONS).query("a", "b"), true);
        assertRequestMatch(request(POST).query("a", "b"), true);
        assertRequestMatch(request(PUT).query("a", "b"), true);
        assertRequestMatch(request(PATCH).query("a", "b"), true);
    }

    @Test @DisplayName("GET(String)")
    void getMethodString() {
        final var request = expectations.GET(PATH);

        assertRequest(request);
        assertRequestMatch(request(GET), true);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
    }

    @Test @DisplayName("GET(Matcher)")
    void getMethodMatcher() {
        final var request = expectations.GET(equalTo(PATH));

        assertRequest(request);
        assertRequestMatch(request(GET), true);
        assertRequestMatch(request(GET, OTHER_PATH), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
    }

    @Test @DisplayName("GET(String,Consumer)")
    void getMethodStringConsumer() {
        final var request = expectations.GET(PATH, QUERY_REQUEST_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(GET).query("a", "b"), true);
        assertRequestMatch(request(GET).query("a", "c"), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @Test @DisplayName("GET(Matcher,Consumer)")
    void getMethodMatcherConsumer() {
        final var request = expectations.GET(equalTo(PATH), QUERY_REQUEST_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(GET).query("a", "b"), true);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(GET, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @Test @DisplayName("HEAD(String)")
    void headMethodString() {
        final var request = expectations.HEAD(PATH);

        assertRequest(request);
        assertRequestMatch(request(HEAD), true);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(DELETE), false);
    }

    @Test @DisplayName("HEAD(Matcher)")
    void headMethodMatcher() {
        final var request = expectations.HEAD(equalTo(PATH));

        assertRequest(request);
        assertRequestMatch(request(HEAD), true);
        assertRequestMatch(request(HEAD, OTHER_PATH), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(DELETE), false);
    }

    @Test @DisplayName("HEAD(String,Consumer)")
    void headMethodStringConsumer() {
        final var request = expectations.HEAD(PATH, QUERY_REQUEST_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(HEAD).query("a", "b"), true);
        assertRequestMatch(request(HEAD).query("a", "c"), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
    }

    @Test @DisplayName("HEAD(Matcher,Consumer)")
    void headMethodMatcherConsumer() {
        final var request = expectations.HEAD(equalTo(PATH), QUERY_REQUEST_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(HEAD).query("a", "b"), true);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(HEAD, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
    }

    @Test @DisplayName("DELETE(String)")
    void deleteMethodString() {
        final var request = expectations.DELETE(PATH);

        assertRequest(request);
        assertRequestMatch(request(DELETE), true);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD), false);
    }

    @Test @DisplayName("DELETE(Matcher)")
    void deleteMethodMatcher() {
        final var request = expectations.DELETE(equalTo(PATH));

        assertRequest(request);
        assertRequestMatch(request(DELETE), true);
        assertRequestMatch(request(DELETE, OTHER_PATH), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD), false);
    }

    @Test @DisplayName("DELETE(String,Consumer)")
    void deleteMethodStringConsumer() {
        final var request = expectations.DELETE(PATH, QUERY_REQUEST_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(DELETE).query("a", "b"), true);
        assertRequestMatch(request(DELETE).query("a", "x"), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
    }

    @Test @DisplayName("DELETE(Matcher,Consumer)")
    void deleteMethodMatcherConsumer() {
        final var request = expectations.DELETE(equalTo(PATH), QUERY_REQUEST_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(DELETE).query("a", "b"), true);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(DELETE, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
    }

    @Test @DisplayName("OPTIONS(String)")
    void optionsMethodString() {
        final var request = expectations.OPTIONS(PATH);

        assertRequest(request);

        assertRequestMatch(request(OPTIONS), true);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
    }

    @Test @DisplayName("OPTIONS(Matcher)")
    void optionsMethodMatcher() {
        final var request = expectations.OPTIONS(equalTo(PATH));

        assertRequest(request);

        assertRequestMatch(request(OPTIONS), true);
        assertRequestMatch(request(OPTIONS, OTHER_PATH), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
    }

    @Test @DisplayName("OPTIONS(Matcher,Consumer)")
    void optionsMethodMatcherConsumer() {
        final var request = expectations.OPTIONS(equalTo(PATH), QUERY_REQUEST_EXPECTATION);

        assertRequest(request);

        assertRequestMatch(request(OPTIONS).query("a", "b"), true);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(OPTIONS, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
    }

    @Test @DisplayName("OPTIONS(String,Consumer)")
    void optionsMethodStringConsumer() {
        final var request = expectations.OPTIONS(PATH, QUERY_REQUEST_EXPECTATION);

        assertRequest(request);

        assertRequestMatch(request(OPTIONS).query("a", "b"), true);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(OPTIONS).query("x", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
    }

    @ParameterizedTest @DisplayName("matching") @MethodSource("matchingProvider")
    void matching(final HttpMethod method, final String path) {
        expectations.ANY("/charlie");
        expectations.POST("/alpha");
        expectations.POST("/bravo");
        expectations.DELETE("/alpha");
        expectations.GET("/alpha");

        ClientRequest cr = new MockClientRequest(method, path);

        ErsatzRequest req = (ErsatzRequest) expectations.findMatch(cr).get();
        assertTrue(req.matches(cr));
    }

    private static Stream<Arguments> matchingProvider() {
        return Stream.of(
            arguments(GET, "/charlie"),
            arguments(POST, "/charlie"),
            arguments(PUT, "/charlie"),
            arguments(PATCH, "/charlie"),
            arguments(DELETE, "/charlie"),
            arguments(GET, "/alpha"),
            arguments(POST, "/alpha"),
            arguments(POST, "/bravo"),
            arguments(DELETE, "/alpha")
        );
    }

    @Test @DisplayName("verification (success)")
    void verificationSuccess() {
        RequestWithContent req = (RequestWithContent) expectations.POST("/alpha").called(equalTo(1));
        ((ErsatzRequestWithContent) req).mark(new MockClientRequest());

        assertTrue(expectations.verify());
    }

    @Test @DisplayName("verification (failure)")
    void verificationFailure() {
        expectations.POST("/alpha").called(equalTo(1));

        final var thrown = assertThrows(IllegalArgumentException.class, () -> {
            expectations.verify();
        });

        assertEquals("Expectations for Expectations (ErsatzRequestWithContent): <POST>, \"/alpha\",  were not met.", thrown.getMessage());
    }

    @Test @DisplayName("POST(String)")
    void postMethodString() {
        final var request = expectations.POST(PATH);

        assertRequestWithContent(request);
        assertRequestMatch(request(POST), true);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(PUT), false);
        assertRequestMatch(request(PATCH), false);
    }

    @Test @DisplayName("POST(Matcher)")
    void postMethodMatcher() {
        final var request = expectations.POST(equalTo(PATH));

        assertRequest(request);
        assertRequestMatch(request(POST), true);
        assertRequestMatch(request(POST, OTHER_PATH), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(PUT), false);
        assertRequestMatch(request(PATCH), false);
    }

    @Test @DisplayName("POST(String,Consumer)")
    void postMethodStringConsumer() {
        final var request = expectations.POST(PATH, QUERY_REQUEST_WITH_CONTENT_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(POST).query("a", "b"), true);
        assertRequestMatch(request(POST).query("a", "c"), false);
        assertRequestMatch(request(POST), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(PUT).query("a", "b"), false);
        assertRequestMatch(request(PATCH).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @Test @DisplayName("POST(Matcher,Consumer)")
    void postMethodMatcherConsumer() {
        final var request = expectations.POST(equalTo(PATH), QUERY_REQUEST_WITH_CONTENT_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(POST).query("a", "b"), true);
        assertRequestMatch(request(POST), false);
        assertRequestMatch(request(POST, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(PUT).query("a", "b"), false);
        assertRequestMatch(request(PATCH).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @Test @DisplayName("PUT(String)")
    void putMethodString() {
        final var request = expectations.PUT(PATH);

        assertRequestWithContent(request);
        assertRequestMatch(request(PUT), true);
        assertRequestMatch(request(POST), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(PATCH), false);
    }

    @Test @DisplayName("PUT(Matcher)")
    void putMethodMatcher() {
        final var request = expectations.PUT(equalTo(PATH));

        assertRequest(request);
        assertRequestMatch(request(PUT), true);
        assertRequestMatch(request(PUT, OTHER_PATH), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(POST), false);
        assertRequestMatch(request(PATCH), false);
    }

    @Test @DisplayName("PUT(String,Consumer)")
    void putMethodStringConsumer() {
        final var request = expectations.PUT(PATH, QUERY_REQUEST_WITH_CONTENT_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(PUT).query("a", "b"), true);
        assertRequestMatch(request(PUT).query("a", "c"), false);
        assertRequestMatch(request(PUT), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(POST).query("a", "b"), false);
        assertRequestMatch(request(PATCH).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @Test @DisplayName("PUT(Matcher,Consumer)")
    void putMethodMatcherConsumer() {
        final var request = expectations.PUT(equalTo(PATH), QUERY_REQUEST_WITH_CONTENT_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(PUT).query("a", "b"), true);
        assertRequestMatch(request(PUT), false);
        assertRequestMatch(request(PUT, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(POST).query("a", "b"), false);
        assertRequestMatch(request(PATCH).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @Test @DisplayName("PATCH(String)")
    void patchMethodString() {
        final var request = expectations.PATCH(PATH);

        assertRequestWithContent(request);
        assertRequestMatch(request(PATCH), true);
        assertRequestMatch(request(POST), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(PUT), false);
    }

    @Test @DisplayName("PATCH(Matcher)")
    void patchMethodMatcher() {
        final var request = expectations.PATCH(equalTo(PATH));

        assertRequest(request);
        assertRequestMatch(request(PATCH), true);
        assertRequestMatch(request(PATCH, OTHER_PATH), false);
        assertRequestMatch(request(HEAD), false);
        assertRequestMatch(request(DELETE), false);
        assertRequestMatch(request(OPTIONS), false);
        assertRequestMatch(request(GET), false);
        assertRequestMatch(request(POST), false);
        assertRequestMatch(request(PUT), false);
    }

    @Test @DisplayName("PATCH(String,Consumer)")
    void patchMethodStringConsumer() {
        final var request = expectations.PATCH(PATH, QUERY_REQUEST_WITH_CONTENT_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(PATCH).query("a", "b"), true);
        assertRequestMatch(request(PATCH).query("a", "c"), false);
        assertRequestMatch(request(PATCH), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(POST).query("a", "b"), false);
        assertRequestMatch(request(PUT).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @Test @DisplayName("PATCH(Matcher,Consumer)")
    void patchMethodMatcherConsumer() {
        final var request = expectations.PATCH(equalTo(PATH), QUERY_REQUEST_WITH_CONTENT_EXPECTATION);

        assertRequest(request);
        assertRequestMatch(request(PATCH).query("a", "b"), true);
        assertRequestMatch(request(PATCH), false);
        assertRequestMatch(request(PATCH, OTHER_PATH).query("a", "b"), false);
        assertRequestMatch(request(HEAD).query("a", "b"), false);
        assertRequestMatch(request(GET).query("a", "b"), false);
        assertRequestMatch(request(POST).query("a", "b"), false);
        assertRequestMatch(request(PUT).query("a", "b"), false);
        assertRequestMatch(request(DELETE).query("a", "b"), false);
        assertRequestMatch(request(OPTIONS).query("a", "b"), false);
    }

    @ParameterizedTest @DisplayName("wildcard path")
    @CsvSource({
        "/alpha",
        "/bravo",
        "/charlie/delta"
    })
    void wildcardPath(final String path) {
        expectations.GET("*");

        ClientRequest cr = new MockClientRequest(GET, path);

        assertTrue(((ErsatzRequest) expectations.findMatch(cr).get()).matches(cr));
    }

    @ParameterizedTest @DisplayName("matching wildcarded any method") @MethodSource("methodPathProvider")
    void matchingWildcardedAny(final HttpMethod method, final String path) {
        expectations.ANY("*");

        ClientRequest cr = new MockClientRequest(method, path);

        assertTrue(((ErsatzRequest) expectations.findMatch(cr).get()).matches(cr));
    }

    private static Stream<Arguments> methodPathProvider() {
        return Stream.of(
            arguments(GET, "/alpha"),
            arguments(POST, "/bravo"),
            arguments(PUT, "/charlie"),
            arguments(PATCH, "/delta"),
            arguments(DELETE, "/echo"),
            arguments(OPTIONS, "/foxtrot")
        );
    }

    private void assertRequest(final Request request) {
        assertTrue(request instanceof ErsatzRequest);
        assertEquals(1, expectations.getRequests().size());
    }

    private void assertRequestWithContent(final Request request) {
        assertTrue(request instanceof ErsatzRequestWithContent);
        assertEquals(1, expectations.getRequests().size());
    }

    private MockClientRequest request(final HttpMethod method) {
        return request(method, PATH);
    }

    private MockClientRequest request(final HttpMethod method, final String path) {
        return new MockClientRequest(method, path);
    }

    private void assertRequestMatch(final ClientRequest test, final boolean matches) {
        assertEquals(matches, ((ErsatzRequest) expectations.getRequests().get(0)).matches(test));
    }
}
