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
import lombok.val;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayDeque;

import static io.github.cjstehno.ersatz.cfg.ContentType.CONTENT_TYPE_HEADER;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsIterableContaining.hasItem;

// FIXME: document
// FIXME: test
public abstract class HeaderMatcher extends BaseMatcher<ClientRequest> {

    // FIXME: test with multiple specified heacer values

    /**
     * FIXME: document
     *
     * @param nameMatcher
     * @param valueMatcher
     * @return
     */
    public static HeaderMatcher headerMatching(final Matcher<String> nameMatcher, final Matcher<Iterable<? super String>> valueMatcher) {
        return new HeaderMatches(nameMatcher, valueMatcher);
    }

    // FIXME: document
    public static HeaderMatcher headerMatching(final String name, final Matcher<Iterable<? super String>> valueMatcher) {
        return new HeaderMatches(equalToIgnoringCase(name), valueMatcher);
    }

    // FIXME: document
    public static HeaderMatcher headerMatching(final String name, final String value) {
        return headerMatching(name, hasItem(value));
    }

    // FIXME: document
    public static HeaderMatcher headerExists(final String name) {
        return headerExists(equalToIgnoringCase(name));
    }

    // FIXME: document
    public static HeaderMatcher headerExists(final Matcher<String> nameMatcher) {
        return new HasHeaderMatching(nameMatcher, false);
    }

    // FIXME: document
    public static HeaderMatcher headerDoesNotExist(final String name) {
        return headerDoesNotExist(equalToIgnoringCase(name));
    }

    // FIXME: document
    public static HeaderMatcher headerDoesNotExist(final Matcher<String> nameMatcher) {
        return new HasHeaderMatching(nameMatcher, true);
    }

    // FIXME: document
    public static HeaderMatcher contentTypeHeader(final String contentType) {
        return headerMatching(CONTENT_TYPE_HEADER, hasItem(startsWith(contentType)));
    }

    // FIXME: these are almost identical to the ones for QueryPAram... share and remove duplication
    @RequiredArgsConstructor(access = PRIVATE)
    private static class HeaderMatches extends HeaderMatcher {

        private final Matcher<String> nameMatcher;
        private final Matcher<Iterable<? super String>> valueMatcher;

        @Override public boolean matches(final Object actual) {
            val clientRequest = (ClientRequest) actual;
            val headers = clientRequest.getHeaders();

            return headers.keySet().stream()
                .filter(nameMatcher::matches)
                .findAny()
                .filter(key -> valueMatcher.matches(new ArrayDeque<>(asList(headers.get(key).toArray(new String[0])))))
                .isPresent();
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
