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

import io.github.cjstehno.ersatz.impl.matchers.MapKeyMatcher;
import io.github.cjstehno.ersatz.impl.matchers.MappedValuesMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.val;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.LinkedList;

import static io.github.cjstehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;

/**
 * Matcher used to match request body parameters (e.g. form posts).
 */
public abstract class BodyParamMatcher extends BaseMatcher<ClientRequest> {

    /**
     * Creates a matcher to match a request body parameter with the given name and value.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamMatching(final String name, final String value) {
        return bodyParamMatching(equalTo(name), hasItem(value));
    }

    /**
     * Creates a matcher to match a request body parameter with the given name and values.
     *
     * @param name   the parameter name
     * @param values the parameter values
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamMatching(final String name, final Iterable<? super String> values) {
        val matchers = new LinkedList<Matcher<? super String>>();
        values.forEach(v -> matchers.add(equalTo(v)));

        return bodyParamMatching(name, stringIterableMatcher(matchers));
    }

    /**
     * Creates a matcher to match a request body parameter with the given name and value matcher.
     *
     * @param name         the parameter name
     * @param valueMatcher the parameter value matcher
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamMatching(final String name, final Matcher<Iterable<? super String>> valueMatcher) {
        return bodyParamMatching(equalTo(name), valueMatcher);
    }

    /**
     * Creates a matcher to match a request body parameter with the given name and value matchers.
     *
     * @param nameMatcher  the parameter name matcher
     * @param valueMatcher the parameter value matcher
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamMatching(final Matcher<String> nameMatcher, final Matcher<Iterable<? super String>> valueMatcher) {
        return new BodyParamMatches(nameMatcher, valueMatcher);
    }

    /**
     * Creates a matcher to match when a request body param exists with the given name.
     *
     * @param name the request body param name
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamExists(final String name) {
        return bodyParamExists(equalTo(name));
    }

    /**
     * Creates a matcher to match when a request body param exists with the given name matcher.
     *
     * @param nameMatcher the request body param name
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamExists(final Matcher<String> nameMatcher) {
        return new HasBodyParamMatching(nameMatcher, false);
    }

    /**
     * Creates a matcher to match when a request body param does not exist with the given name.
     *
     * @param name the request body param name
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamDoesNotExist(final String name) {
        return bodyParamDoesNotExist(equalTo(name));
    }

    /**
     * Creates a matcher to match when a request body param does not exist with the given name matcher.
     *
     * @param nameMatcher the request body param name
     * @return the body param matcher
     */
    public static BodyParamMatcher bodyParamDoesNotExist(final Matcher<String> nameMatcher) {
        return new HasBodyParamMatching(nameMatcher, true);
    }

    private static class BodyParamMatches extends BodyParamMatcher {

        private final MappedValuesMatcher matcherDelegate;

        private BodyParamMatches(final Matcher<String> nameMatcher, final Matcher<Iterable<? super String>> valueMatcher) {
            matcherDelegate = new MappedValuesMatcher(
                "Request body param", nameMatcher, valueMatcher, ClientRequest::getBodyParameters
            );
        }

        @Override public boolean matches(final Object actual) {
            return matcherDelegate.matches(actual);
        }

        @Override public void describeTo(final Description description) {
            matcherDelegate.describeTo(description);
        }
    }

    private static class HasBodyParamMatching extends BodyParamMatcher {

        private final MapKeyMatcher matcherDelegate;

        private HasBodyParamMatching(final Matcher<String> nameMatcher, final boolean negated) {
            matcherDelegate = new MapKeyMatcher("Body param", nameMatcher, negated, ClientRequest::getBodyParameters);
        }

        @Override public boolean matches(final Object actual) {
            return matcherDelegate.matches(actual);
        }

        @Override public void describeTo(final Description description) {
            matcherDelegate.describeTo(description);
        }
    }
}