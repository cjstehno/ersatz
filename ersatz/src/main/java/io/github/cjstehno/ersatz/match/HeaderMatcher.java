/**
 * Copyright (C) 2022 Christopher J. Stehno
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

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayDeque;

import static io.github.cjstehno.ersatz.cfg.ContentType.CONTENT_TYPE_HEADER;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.IsIterableContaining.hasItem;

/**
 * Matcher used to match a request header.
 */
public abstract class HeaderMatcher extends BaseMatcher<ClientRequest> {

    /**
     * Creates a matcher for a request header that matches the given name matcher and value matcher.
     *
     * @param nameMatcher  the name matcher
     * @param valueMatcher the value matcher
     * @return the header matcher
     */
    public static HeaderMatcher headerMatching(final Matcher<String> nameMatcher, final Matcher<Iterable<? super String>> valueMatcher) {
        return new HeaderMatches(nameMatcher, valueMatcher);
    }

    /**
     * Creates a matcher for a request header that matches the given name and provided value matcher.
     *
     * @param name         the name
     * @param valueMatcher the value matcher
     * @return the header matcher
     */
    public static HeaderMatcher headerMatching(final String name, final Matcher<Iterable<? super String>> valueMatcher) {
        return headerMatching(equalToIgnoringCase(name), valueMatcher);
    }

    /**
     * Creates a matcher for a request header that matches the given name and value.
     *
     * @param name  the name
     * @param value the value
     * @return the header matcher
     */
    public static HeaderMatcher headerMatching(final String name, final String value) {
        return headerMatching(name, hasItem(value));
    }

    /**
     * Creates a matcher that matches when a header exists with the given name.
     *
     * @param name the name
     * @return the header matcher
     */
    public static HeaderMatcher headerExists(final String name) {
        return headerExists(equalToIgnoringCase(name));
    }

    /**
     * Creates a matcher that matches when a header exists matching the given name matcher.
     *
     * @param nameMatcher the name matcher
     * @return the header matcher
     */
    public static HeaderMatcher headerExists(final Matcher<String> nameMatcher) {
        return new HasHeaderMatching(nameMatcher, false);
    }

    /**
     * Creates a matcher that matches when a header does not exist with the given name.
     *
     * @param name the name
     * @return the header matcher
     */
    public static HeaderMatcher headerDoesNotExist(final String name) {
        return headerDoesNotExist(equalToIgnoringCase(name));
    }

    /**
     * Creates a matcher that matches when a header does not exist matching the given name matcher.
     *
     * @param nameMatcher the name matcher
     * @return the header matcher
     */
    public static HeaderMatcher headerDoesNotExist(final Matcher<String> nameMatcher) {
        return new HasHeaderMatching(nameMatcher, true);
    }

    /**
     * Creates a matcher that matches a Content-Type header with the specified value.
     *
     * @param contentType the content type value
     * @return the header matcher
     */
    public static HeaderMatcher contentTypeHeader(final String contentType) {
        return contentTypeHeader(equalTo(contentType));
    }

    /**
     * Creates a matcher that matches a Content-Type header matching the provided matcher.
     *
     * @param contentTypeMatcher the content type value matcher
     * @return the header matcher
     */
    public static HeaderMatcher contentTypeHeader(final Matcher<String> contentTypeMatcher) {
        return headerMatching(CONTENT_TYPE_HEADER, hasItem(contentTypeMatcher));
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class HeaderMatches extends HeaderMatcher {

        private final Matcher<String> nameMatcher;
        private final Matcher<Iterable<? super String>> valueMatcher;

        @Override public boolean matches(final Object actual) {
            return ((ClientRequest) actual).getHeaders().entrySet().stream()
                .filter(ent -> nameMatcher.matches(ent.getKey()))
                .anyMatch(ent -> valueMatcher.matches(new ArrayDeque<>(asList(ent.getValue().toArray(new String[0])))));
        }

        @Override public void describeTo(Description description) {
            description.appendText("Header name is ");
            nameMatcher.describeTo(description);
            description.appendText(" and value is ");
            valueMatcher.describeTo(description);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class HasHeaderMatching extends HeaderMatcher {

        private final Matcher<String> nameMatcher;
        private final boolean negated;

        @Override public boolean matches(final Object actual) {
            return negated != ((ClientRequest) actual).getHeaders().keySet().stream().anyMatch(nameMatcher::matches);
        }

        @Override public void describeTo(Description description) {
            description.appendText("Header name is ");
            nameMatcher.describeTo(description);
        }
    }
}
