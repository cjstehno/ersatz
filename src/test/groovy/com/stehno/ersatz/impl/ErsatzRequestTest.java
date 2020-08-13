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

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.cfg.Response;
import com.stehno.ersatz.encdec.ResponseEncoders;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.server.ClientRequest;
import com.stehno.ersatz.server.MockClientRequest;
import com.stehno.ersatz.util.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import static com.stehno.ersatz.cfg.HttpMethod.POST;
import static com.stehno.ersatz.encdec.Cookie.cookie;
import static com.stehno.ersatz.match.CookieMatcher.cookieMatcher;
import static com.stehno.ersatz.match.NoCookiesMatcher.noCookies;
import static com.stehno.ersatz.server.UnderlyingServer.NOT_FOUND_BODY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(ErsatzServerExtension.class)
class ErsatzRequestTest {

    private static final String STRING_CONTENT = "Some content";
    private ErsatzServer server;
    private HttpClient http;
    private ErsatzRequest request;

    @BeforeEach void beforeEach() {
        http = new HttpClient();
        request = new ErsatzRequest(POST, equalTo("/testing"), new ResponseEncoders());
    }

    @Test @DisplayName("to string")
    void string() {
        assertEquals("Expectations (ErsatzRequest): <POST>, \"/testing\", ", request.toString());
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
    void queryWithNullValue(final ClientRequest cr, final boolean result) {
        request.query("enabled", (String) null);

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> queriesWithNullProvider() {
        return Stream.of(
            arguments(clientRequest().query("enabled", ""), true),
            arguments(clientRequest().query("enabled", "yes"), false),
            arguments(clientRequest().query("disabled", ""), false)
        );
    }

    @Disabled("FIXME: this may be a bug")
    @ParameterizedTest(name = "{index} query({0},{1}) -> {3})") @DisplayName("query with no value")
    @CsvSource({
        "enabled,,true",
        "enabled,yes,false",
        "disabled,,false"
    })
    void queryWithNoValue(final String name, final String value, final boolean result) {
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

    @ParameterizedTest @DisplayName("cookies") @MethodSource("cookiesProvider")
    void cookies(final ClientRequest cr, final boolean result) {
        request.cookies(
            Map.of(
                "chocolate", "yes",
                "amount", "dozen"
            )
        ).cookie("sugar", "no");

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> cookiesProvider() {
        return Stream.of(
            arguments(clientRequest().cookie("amount", "dozen").cookie("sugar", "no"), false),
            arguments(clientRequest().cookie("chocolate", "yes").cookie("amount", "dozen").cookie("sugar", "no"), true),
            arguments(clientRequest().cookie("chocolate", "yes").cookie("amount", "dozen").cookie("sugar", "no").cookie("more", "fun"), true)
        );
    }

    @ParameterizedTest @DisplayName("ensure matcher cookies") @MethodSource("matcherCookiesProvider")
    void ensureMatcherCookies(final ClientRequest cr, final boolean result) {
        request.cookies(Map.of(
            "foo", cookieMatcher(m -> m.value("one")),
            "bar", cookieMatcher(m -> m.value(equalTo("two")))
        ));

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> matcherCookiesProvider() {
        return Stream.of(
            arguments(clientRequest().cookie("foo", "one").cookie("bar", "two"), true),
            arguments(clientRequest().cookie("foo", "one").cookie("bar", "blah"), false)
        );
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

    @ParameterizedTest @DisplayName("match one cookie present and one not") @MethodSource("cookieOrNotCookieProvider")
    void cookieOrNotCookie(final ClientRequest cr, final boolean result) {
        request.cookies(Map.of(
            "foo", nullValue(),
            "bar", cookieMatcher(m -> m.value(equalTo("two")))
        ));

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> cookieOrNotCookieProvider() {
        return Stream.of(
            arguments(clientRequest().cookie("foo", "one").cookie("bar", "two"), false),
            arguments(clientRequest().cookie("bar", "two"), true)
        );
    }

    @ParameterizedTest @DisplayName("match with no cookies") @MethodSource("noCookiesProvider")
    void matchWithNoCookies(final ClientRequest cr, final boolean result) {
        request.cookies(noCookies());

        assertEquals(result, request.matches(cr));
    }

    private static Stream<Arguments> noCookiesProvider() {
        return Stream.of(
            arguments(clientRequest(), true),
            arguments(clientRequest().cookie("alpha", "bravo"), false)
        );
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

        final var response = http.get(server.httpUrl("/test"));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: header")
    void matchingHeader() throws IOException {
        server.expectations(e -> {
            e.GET("/test").header("one", "blah").responds().body(STRING_CONTENT);
        });

        var response = http.get(Map.of("one", "blah"), server.httpUrl("/test"));
        assertEquals(STRING_CONTENT, response.body().string());

        response = http.get(server.httpUrl("/test"));

        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: headers")
    void matchingHeaders() throws IOException {
        server.expectations(e -> {
            e.GET("/test", r -> {
                r.headers(Map.of(
                    "alpha", "one",
                    "bravo", "two"
                ));
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = http.get(
            Map.of(
                "alpha", "one",
                "bravo", "two"
            ),
            server.httpUrl("/test")
        );

        assertEquals(STRING_CONTENT, response.body().string());

        response = http.get(
            Map.of(
                "alpha", "one"
            ),
            server.httpUrl("/test")
        );

        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: query")
    void matchingQuery() throws IOException {
        server.expectations(e -> {
            e.GET("/testing").query("alpha", "blah").responds().body(STRING_CONTENT);
        });

        var response = http.get(server.httpUrl("/testing?alpha=blah"));
        assertEquals(STRING_CONTENT, response.body().string());

        response = http.get(server.httpUrl("/testing"));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: queries")
    void matchingQueries() throws IOException {
        server.expectations(e -> {
            e.GET("/testing", r -> {
                r.queries(Map.of(
                    "alpha", List.of("one"),
                    "bravo", List.of("two", "three")
                ));
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = http.get(server.httpUrl("/testing?alpha=one&bravo=two&bravo=three"));
        assertEquals(STRING_CONTENT, response.body().string());

        response = http.get(server.httpUrl("/testing"));
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

        var response = http.get(
            Map.of("Cookie", "flavor=chocolate-chip"),
            server.httpUrl("/test")
        );
        assertEquals(STRING_CONTENT, response.body().string());

        response = http.get(server.httpUrl("/test"));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: cookies")
    void matchingCookies() throws IOException {
        server.expectations(e -> {
            e.GET("/test", r -> {
                r.cookies(Map.of(
                    "flavor", "chocolate-chip"
                ));
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = http.get(
            Map.of("Cookie", "flavor=chocolate-chip"),
            server.httpUrl("/test")
        );
        assertEquals(STRING_CONTENT, response.body().string());

        response = http.get(server.httpUrl("/test"));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("generic matcher")
    void genericMatcher() throws IOException {
        server.expectations(e -> {
            e.GET("/testing", r -> {
                r.matcher(allOf(
                    hasProperty("protocol", equalTo("http")),
                    hasProperty("path", equalTo("/testing")),
                    hasProperty("queryParams", not(anEmptyMap()))
                ));
                r.responds().body(STRING_CONTENT);
            });
        });

        var response = http.get(server.httpUrl("/testing?alpha=123"));
        assertEquals(STRING_CONTENT, response.body().string());

        response = http.get(server.httpUrl("/testing"));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    private static MockClientRequest clientRequest() {
        final var req = new MockClientRequest(POST);
        req.setPath("/testing");
        return req;
    }
}
