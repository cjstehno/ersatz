/**
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.Response;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.server.ClientRequest;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.POST;
import static io.github.cjstehno.ersatz.encdec.Cookie.cookie;
import static io.github.cjstehno.ersatz.match.CookieMatcher.cookieMatcher;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static io.github.cjstehno.ersatz.server.UnderlyingServer.NOT_FOUND_BODY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
class ErsatzRequestTest {

    private static final String STRING_CONTENT = "Some content";
    private ErsatzServer server;
    private HttpClientExtension.Client client;
    private ErsatzRequest request;

    @BeforeEach void beforeEach() {
        request = new ErsatzRequest(POST, pathMatching("/testing"), new ResponseEncoders(), false);
    }

    @Test @DisplayName("to string")
    void string() {
        assertEquals("Expectations (ErsatzRequest): HTTP method is (<POST>), Path matches \"/testing\", and Called ANYTHING", request.toString());
    }

    @Test @DisplayName("method and path")
    void methodAndPath() {
        assertTrue(request.matches(clientRequest()));
    }

    @ParameterizedTest @DisplayName("headers") @MethodSource("headersProvider")
    void headers(final ClientRequest cr, final boolean result) {
        request.headers(
            Map.of(
                "alpha", "bravo",
                "charlie", "delta"
            )
        ).header("echo", "foxtrot");

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> headersProvider() {
        return Stream.of(
            arguments(clientRequest().header("alpha", "bravo").header("charlie", "delta").header("echo", "foxtrot"), true),
            arguments(clientRequest().header("alpha", "bravo").header("echo", "foxtrot"), false),
            arguments(clientRequest().header("alpha", "bravo").header("charlie", "delta").header("echo", "foxtrot").header("nothing", "nowhere"), true),
            arguments(clientRequest().header("alpha", "bravo").header("charlie", "not-right").header("echo", "foxtrot"), false)
        );
    }

    @ParameterizedTest @DisplayName("queries") @MethodSource("queriesProvider")
    void queries(final ClientRequest cr, final boolean result) {
        request.queries(
            Map.of(
                "one", List.of("two"),
                "three", List.of("four", "five")
            )
        ).query("foo", "bar");

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> queriesProvider() {
        return Stream.of(
            arguments(clientRequest().query("one", "two").query("three", "four", "five").query("foo", "bar"), true),
            arguments(clientRequest().query("one", "two").query("three", "four", "five"), false),
            arguments(clientRequest().query("one", "two").query("three", "xyz", "five").query("foo", "bar"), false)
        );
    }

    @ParameterizedTest @DisplayName("query with null value") @MethodSource("queriesWithNullProvider")
    void queryExists(final ClientRequest cr, final boolean result) {
        request.query("enabled");

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> queriesWithNullProvider() {
        return Stream.of(
            arguments(clientRequest().query("enabled", ""), true),
            arguments(clientRequest().query("enabled", "yes"), true),
            arguments(clientRequest().query("disabled", ""), false),
            arguments(clientRequest().query("enabled", (String) null), true)
        );
    }

    @ParameterizedTest(name = "{index} query({0},{1}) -> {2})")
    @CsvSource({
        "enabled,,true",
        "enabled,yes,true",
        "disabled,,false"
    })
    void queryExists(final String name, final String value, final boolean result) {
        request.query("enabled");

        assertEquals(result, request.matches(clientRequest().query(name, value)));
    }

    @ParameterizedTest @DisplayName("queries with no value")
    @CsvSource({
        "enabled,,true",
        "enabled,yes,false"
    })
    void queriesWithNoValue(final String name, final String value, final boolean result) {
        request.queries(Map.of("enabled", List.of()));

        assertEquals(result, request.matches(clientRequest().query(name, value)));
    }

    @ParameterizedTest @DisplayName("cookie equals matching")
    @CsvSource({
        "bar,boo-boo,false",
        "bar,blah-blah,true"
    })
    void cookieEqualsMatching(final String name, final String value, final boolean result) {
        request.cookie("bar", equalTo(cookie(c -> c.value("blah-blah"))));

        assertEquals(result, request.matches(clientRequest().cookie(name, value)));
    }

    @ParameterizedTest @DisplayName("deep cookie properties") @MethodSource("deepCookieProvider")
    void deepCookieProperties(final ClientRequest cr, final boolean result) {
        request.cookie("foo", cookieMatcher(m -> {
            m.value("alpha");
            m.comment("a comment");
            m.domain("blah.com");
            m.path("/some/path");
            m.maxAge(12345);
            m.version(1);
            m.httpOnly(true);
            m.secure(true);
        }));

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> deepCookieProvider() {
        return Stream.of(
            arguments(clientRequest().cookie("foo", "alpha"), false),
            arguments(clientRequest().cookie("foo", "alpha", "a comment", "blah.com", "/some/path", 12345, true, true, 1), true)
        );
    }

    @Test @DisplayName("listener")
    void listener() {
        AtomicInteger counter = new AtomicInteger(0);

        request.listener(r -> counter.incrementAndGet());

        ClientRequest cr = clientRequest();

        request.mark(cr);
        request.mark(cr);

        assertEquals(2, counter.get());
    }

    @ParameterizedTest @DisplayName("expected calls")
    @CsvSource({
        "2,1,false",
        "2,2,true",
        "2,3,false"
    })
    void expectedCalls(final int expected, final int calls, final boolean verified) {
        request.called(equalTo(expected));

        for (int t = 0; t < calls; t++) {
            request.mark(clientRequest());
        }

        assertEquals(verified, request.verify(1, SECONDS));
    }

    @Test @DisplayName("responds")
    void responds() {
        String body = "the-body";

        request.responds().contentType("something/else").body(body);

        Response resp = request.getCurrentResponse();
        assertEquals("something/else", resp.getContentType());
        assertArrayEquals(body.getBytes(), resp.getContent());
    }

    @Test @DisplayName("responder")
    void responder() {
        Object contentA = "body-A";
        Object contentB = "body-B";

        request.responds().contentType("something/else").body(contentA);
        request.responder(r -> {
            r.contentType("test/date");
            r.body(contentB);
        });

        Response resp = request.getCurrentResponse();

        assertEquals("something/else", resp.getContentType());
        assertArrayEquals(contentA.toString().getBytes(), resp.getContent());

        request.mark(clientRequest());
        resp = request.getCurrentResponse();

        assertEquals("test/date", resp.getContentType());
        assertArrayEquals(contentB.toString().getBytes(), resp.getContent());

        request.mark(clientRequest());
        resp = request.getCurrentResponse();

        assertEquals("test/date", resp.getContentType());
        assertArrayEquals(contentB.toString().getBytes(), resp.getContent());
    }

    @Test @DisplayName("matching: not found")
    void matchingNotFound() throws IOException {
        server.expectations(e -> {
            e.GET("/blah").responds().body(new Object());
        });

        final var response = client.get("/test");
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: header")
    void matchingHeader() throws IOException {
        server.expectations(e -> {
            e.GET("/test").header("one", "blah").responds().body(STRING_CONTENT);
        });

        var response = client.get("/test", builder -> builder.header("one", "blah"));
        assertEquals(STRING_CONTENT, response.body().string());

        response = client.get("/test");

        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: headers")
    void matchingHeaders() throws IOException {
        server.expectations(e -> {
            e.GET("/test", r -> {
                r.headers(Map.of(
                    "alpha", "one",
                    "bravo", IsIterableContaining.hasItem("two")
                ));
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = client.get(
            "/test",
            builder -> {
                builder.header("alpha", "one");
                builder.header("bravo", "two");
            }
        );

        assertEquals(STRING_CONTENT, response.body().string());

        response = client.get(
            "/test",
            builder -> builder.header("alpha", "one")
        );

        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: query")
    void matchingQuery() throws IOException {
        server.expectations(e -> {
            e.GET("/testing").query("alpha", "blah").responds().body(STRING_CONTENT);
        });

        var response = client.get("/testing?alpha=blah");
        assertEquals(STRING_CONTENT, response.body().string());

        response = client.get("/testing");
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: queries")
    void matchingQueries() throws IOException {
        server.expectations(e -> {
            e.GET("/testing", r -> {
                r.queries(Map.of(
                    "alpha", List.of("one"),
                    "bravo", IsIterableContaining.hasItems("two", "three"),
                    "charlie", "four"
                ));
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = client.get("/testing?alpha=one&bravo=two&bravo=three&charlie=four");
        assertEquals(STRING_CONTENT, response.body().string());

        response = client.get("/testing");
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: cookie")
    void matchingCookie() throws IOException {
        server.expectations(e -> {
            e.GET("/test", r -> {
                r.cookie("flavor", "chocolate-chip");
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = client.get(
            "/test",
            builder -> builder.header("Cookie", "flavor=chocolate-chip")
        );
        assertEquals(STRING_CONTENT, response.body().string());

        response = client.get("/test");
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("generic matcher")
    void genericMatcher() throws IOException {
        server.expectations(e -> {
            e.GET("/testing", r -> {
                r.matcher(allOf(
                    hasProperty("scheme", equalTo("http")),
                    hasProperty("path", equalTo("/testing")),
                    hasProperty("queryParams", not(anEmptyMap()))
                ));
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = client.get("/testing?alpha=123");
        assertEquals(STRING_CONTENT, response.body().string());

        response = client.get("/testing");
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    private static MockClientRequest clientRequest() {
        final var req = new MockClientRequest(POST);
        req.setPath("/testing");
        return req;
    }
}
