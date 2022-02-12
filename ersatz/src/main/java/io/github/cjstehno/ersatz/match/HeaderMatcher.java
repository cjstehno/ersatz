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

import io.github.cjstehno.ersatz.impl.matchers.MapKeyMatcher;
import io.github.cjstehno.ersatz.impl.matchers.MappedValuesMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static io.github.cjstehno.ersatz.cfg.ContentType.CONTENT_TYPE_HEADER;
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

    private static class HeaderMatches extends HeaderMatcher {

        private final MappedValuesMatcher matcherDelegate;

        private HeaderMatches(final Matcher<String> nameMatcher, final Matcher<Iterable<? super String>> valueMatcher) {
            matcherDelegate = new MappedValuesMatcher(
                "Request header", nameMatcher, valueMatcher, ClientRequest::getHeaders
            );
        }

        @Override public boolean matches(final Object actual) {
            return matcherDelegate.matches(actual);
        }

        @Override public void describeTo(final Description description) {
            matcherDelegate.describeTo(description);
        }
    }

    private static class HasHeaderMatching extends HeaderMatcher {

        private final MapKeyMatcher matcherDelegate;

        private HasHeaderMatching(final Matcher<String> nameMatcher, final boolean negated) {
            matcherDelegate = new MapKeyMatcher("Header", nameMatcher, negated, ClientRequest::getHeaders);
        }

        @Override public boolean matches(final Object actual) {
            return matcherDelegate.matches(actual);
        }

        @Override public void describeTo(final Description description) {
            matcherDelegate.describeTo(description);
        }
    }
}
