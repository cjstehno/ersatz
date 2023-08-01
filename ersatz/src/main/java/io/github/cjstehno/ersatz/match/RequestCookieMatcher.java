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

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.equalTo;

import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Matcher used to match request cookie information.
 */
public abstract class RequestCookieMatcher extends BaseMatcher<ClientRequest> {

    /**
     * Creates a request cookie matcher for matching the given name and value.
     *
     * @param name  the cookie name
     * @param value the cookie value
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher cookieMatching(final String name, final String value) {
        return cookieMatching(name, new CookieMatcher().value(value));
    }

    /**
     * Creates a request cookie matcher for matching the given name and cookie matcher.
     *
     * @param name          the cookie name
     * @param cookieMatcher the cookie matcher
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher cookieMatching(final String name, final Matcher<Cookie> cookieMatcher) {
        return cookieMatching(equalTo(name), cookieMatcher);
    }

    /**
     * Creates a request cookie matcher for matching the given name matcher and cookie matcher.
     *
     * @param nameMatcher   the cookie name matcher
     * @param cookieMatcher the cookie matcher
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher cookieMatching(final Matcher<String> nameMatcher, final Matcher<Cookie> cookieMatcher) {
        return new CookieMatches(nameMatcher, cookieMatcher);
    }

    /**
     * Creates a request cookie matcher that matches when a cookie exists with the given name.
     *
     * @param name the cookie name
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher cookieExists(final String name) {
        return cookieExists(equalTo(name));
    }

    /**
     * Creates a request cookie matcher that matches when a cookie exists matching the given name matcher.
     *
     * @param nameMatcher the cookie name
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher cookieExists(final Matcher<String> nameMatcher) {
        return new HasCookieMatching(nameMatcher, false);
    }

    /**
     * Creates a request cookie matcher that matches when a cookie does not exist with the given name.
     *
     * @param name the cookie name
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher cookieDoesNotExist(final String name) {
        return cookieDoesNotExist(equalTo(name));
    }

    /**
     * Creates a request cookie matcher that matches when a cookie does not exist matching the given name matcher.
     *
     * @param nameMatcher the cookie name
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher cookieDoesNotExist(final Matcher<String> nameMatcher) {
        return new HasCookieMatching(nameMatcher, true);
    }

    /**
     * Creates a request cookie matcher that matches when the request contains no cookies.
     *
     * @return the request cookie matcher
     */
    public static RequestCookieMatcher hasNoCookies() {
        return new HasNoCookies();
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class CookieMatches extends RequestCookieMatcher {

        private final Matcher<String> nameMatcher;
        private final Matcher<Cookie> cookieMatcher;

        @Override public boolean matches(final Object actual) {
            return ((ClientRequest) actual).getCookies().entrySet().stream()
                .filter(ent -> nameMatcher.matches(ent.getKey()))
                .anyMatch(ent -> cookieMatcher.matches(ent.getValue()));
        }

        @Override public void describeTo(Description description) {
            description.appendText("Cookie name is ");
            nameMatcher.describeTo(description);
            description.appendText(" and is ");
            cookieMatcher.describeTo(description);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class HasCookieMatching extends RequestCookieMatcher {

        private final Matcher<String> nameMatcher;
        private final boolean negated;

        @Override public boolean matches(final Object actual) {
            return negated != ((ClientRequest) actual).getCookies().keySet().stream().anyMatch(nameMatcher::matches);
        }

        @Override public void describeTo(Description description) {
            description.appendText("Cookie name is ");
            nameMatcher.describeTo(description);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class HasNoCookies extends RequestCookieMatcher {

        @Override public boolean matches(final Object actual) {
            return ((ClientRequest) actual).getCookies().isEmpty();
        }

        @Override public void describeTo(final Description description) {
            description.appendText("Has no cookies.");
        }
    }
}
