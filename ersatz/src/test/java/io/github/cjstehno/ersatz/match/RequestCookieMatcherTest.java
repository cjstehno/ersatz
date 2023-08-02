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
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.match.RequestCookieMatcher.cookieDoesNotExist;
import static io.github.cjstehno.ersatz.match.RequestCookieMatcher.cookieExists;
import static io.github.cjstehno.ersatz.match.RequestCookieMatcher.cookieMatching;
import static io.github.cjstehno.ersatz.match.RequestCookieMatcher.hasNoCookies;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RequestCookieMatcherTest {

    private MockClientRequest request;

    @BeforeEach void beforeEach() {
        request = new MockClientRequest(GET, "/testing")
            .cookie("alpha", "something", "a cookie", "localhost", "/", 12345, true, true, 1)
            .cookie("bravo", "other", "another cookie", "localhost", "/stuff", 24680, false, false, 2);
    }

    @ParameterizedTest @DisplayName("cookie") @MethodSource("cookieProvider")
    void cookie(final MockClientRequest request, final boolean result) {
        assertEquals(result, cookieMatching("id", new CookieMatcher().value(equalTo("asdf89s7g"))).matches(request));
    }

    private static Stream<Arguments> cookieProvider() {
        return Stream.of(
            arguments(new MockClientRequest().cookie("id", "asdf89s7g"), true),
            arguments(new MockClientRequest().cookie("id", "assdfsdf"), false),
            arguments(new MockClientRequest(), false)
        );
    }

    @Test @DisplayName("matching when no cookies")
    void matchingNoCookies() {
        assertFalse(hasNoCookies().matches(request));
        assertTrue(hasNoCookies().matches(new MockClientRequest(GET, "/foo")));
    }

    @Test @DisplayName("Matching cookie (string, string)")
    void matchingCookieWithStringString() {
        assertTrue(cookieMatching("alpha", "something").matches(request));
        assertFalse(cookieMatching("alpha", "nothing").matches(request));
        assertTrue(cookieMatching("bravo", "other").matches(request));
    }

    @Test @DisplayName("Matching cookie (string, matcher)")
    void matchingCookieWithStringMatcher() {
        assertTrue(cookieMatching("alpha", new CookieMatcher().value("something")).matches(request));
        assertFalse(cookieMatching("alpha", new CookieMatcher().value("nothing")).matches(request));
        assertTrue(cookieMatching("bravo", new CookieMatcher().comment("another cookie")).matches(request));
    }

    @Test @DisplayName("Matching cookie (matcher, matcher)")
    void matchingCookieWithMatcherMatcher() {
        assertTrue(cookieMatching(equalTo("alpha"), new CookieMatcher().value("something")).matches(request));
        assertFalse(cookieMatching(equalTo("alpha"), new CookieMatcher().value("nothing")).matches(request));
        assertTrue(cookieMatching(startsWith("bra"), new CookieMatcher().comment("another cookie")).matches(request));
    }

    @Test @DisplayName("Matching when a cookie exists (string)")
    void matchingCookieExistsWithString() {
        assertTrue(cookieExists("alpha").matches(request));
        assertTrue(cookieExists("bravo").matches(request));
        assertFalse(cookieExists("charlie").matches(request));
    }

    @Test @DisplayName("Matching when a cookie exists (matcher)")
    void matchingCookieExistsWithMatcher() {
        assertTrue(cookieExists(equalTo("alpha")).matches(request));
        assertTrue(cookieExists(startsWith("bra")).matches(request));
        assertFalse(cookieExists(equalTo("charlie")).matches(request));
    }

    @Test @DisplayName("Matching when a cookie doesnt exist (string)")
    void matchingCookieNotExistsWithString() {
        assertFalse(cookieDoesNotExist("alpha").matches(request));
        assertFalse(cookieDoesNotExist("bravo").matches(request));
        assertTrue(cookieDoesNotExist("charlie").matches(request));
    }

    @Test @DisplayName("Matching when a cookie doesnt exist (matcher)")
    void matchingCookieNotExistsWithMatcher() {
        assertFalse(cookieDoesNotExist(equalTo("alpha")).matches(request));
        assertFalse(cookieDoesNotExist(startsWith("bra")).matches(request));
        assertTrue(cookieDoesNotExist(equalTo("charlie")).matches(request));
    }
}