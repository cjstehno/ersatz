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

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;

/**
 * Matcher used to match request query parameters.
 */
public abstract class QueryParamMatcher extends BaseMatcher<ClientRequest> {

    /**
     * Configures a matcher that matches when the provided name and value matcher match the param name and value
     * respectively.
     *
     * @param nameMatcher  the param name matcher
     * @param valueMatcher the param value matcher
     * @return the query param matcher
     */
    public static QueryParamMatcher queryMatching(final Matcher<String> nameMatcher, final Matcher<Iterable<? super String>> valueMatcher) {
        return new QueryParamMatches(nameMatcher, valueMatcher);
    }

    /**
     * Configures a matcher that matches when there is a request query param with the given name that also matches the
     * provided value matcher.
     *
     * @param name         the param name matcher
     * @param valueMatcher the param value matcher
     * @return the query param matcher
     */
    public static QueryParamMatcher queryMatching(final String name, final Matcher<Iterable<? super String>> valueMatcher) {
        return queryMatching(equalTo(name), valueMatcher);
    }

    /**
     * Configures a matcher that matches when there is a request query param with the given name that also has the
     * specified value (as at least one of its values).
     *
     * @param name  the query param name (must not be null)
     * @param value the query param value (must not be null)
     * @return the query param matcher
     */
    public static QueryParamMatcher queryMatching(final String name, final String value) {
        return queryMatching(name, hasItem(value));
    }

    /**
     * Configures a matcher that matches when a query param with the provided name exists in the request.
     *
     * @param name the param name
     * @return the query param matcher
     */
    public static QueryParamMatcher queryExists(final String name) {
        return new QueryHasParamMatching(equalTo(name), false);
    }

    /**
     * Configures a matcher that matches when a query param with the provided name does not exist in the request.
     *
     * @param name the param name
     * @return the query param matcher
     */
    public static QueryParamMatcher queryDoesNotExist(final String name) {
        return new QueryHasParamMatching(equalTo(name), true);
    }

    /**
     * Configures a matcher that matches when a query param in the request has a name that is matched by the provided
     * matcher.
     *
     * @param nameMatcher the name matcher
     * @return the query param matcher
     */
    public static QueryParamMatcher queryHasParamMatching(final Matcher<String> nameMatcher) {
        return new QueryHasParamMatching(nameMatcher, false);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class QueryParamMatches extends QueryParamMatcher {

        private final Matcher<String> nameMatcher;
        private final Matcher<Iterable<? super String>> valueMatcher;

        @Override public boolean matches(final Object actual) {
            val clientRequest = (ClientRequest) actual;
            val queryParams = clientRequest.getQueryParams();

            return queryParams.keySet().stream()
                .filter(nameMatcher::matches)
                .findAny()
                .filter(key -> valueMatcher.matches(new ArrayDeque<>(asList(queryParams.get(key).toArray(new String[0])))))
                .isPresent();
        }

        @Override public void describeTo(final Description description) {
            description.appendText("Query param key is ");
            nameMatcher.describeTo(description);
            description.appendText(" and value is ");
            valueMatcher.describeTo(description);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class QueryHasParamMatching extends QueryParamMatcher {

        private final Matcher<String> nameMatcher;
        private final boolean negated;

        @Override public boolean matches(final Object actual) {
            return negated != ((ClientRequest) actual).getQueryParams().keySet().stream().anyMatch(nameMatcher::matches);
        }

        @Override public void describeTo(Description description) {
            description.appendText("Query param key is ");
            nameMatcher.describeTo(description);
        }
    }
}
