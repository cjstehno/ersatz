/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz

import groovy.transform.CompileStatic
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 * Some reusable Hamcrest matchers useful in Ersatz expectations.
 */
@CompileStatic
class ErsatzMatchers {

    /**
     * The provided matcher must match at least one element of the target collection (as Iterable).
     *
     * @param matcher the matcher to be wrapped
     * @return the wrapping matcher
     */
    static <T> Matcher<Iterable<T>> collectionContainsMatch(final Matcher<T> matcher) {
        new CollectionContainsMatchMatcher<T>(matcher: matcher)
    }

    /**
     * The provided value must match at least one of the elements in the target collection (as Iterable).
     *
     * @param value the value
     * @return the resulting matcher
     */
    static <T> Matcher<Iterable<T>> collectionContains(final T value) {
        new CollectionContainsValueMatcher<T>(value: value)
    }

    @CompileStatic
    private static class CollectionContainsMatchMatcher<T> extends BaseMatcher<Iterable<T>> {

        Matcher<T> matcher

        @Override
        boolean matches(final Object item) {
            (item as Iterable<T>)?.any { s ->
                matcher.matches(s)
            }
        }

        @Override
        void describeTo(Description description) {
            description.appendText('A collection matching ')
            description.appendDescriptionOf(matcher)
        }
    }

    @CompileStatic
    private static class CollectionContainsValueMatcher<T> extends BaseMatcher<Iterable<T>> {

        T value

        @Override
        boolean matches(final Object item) {
            (item as Iterable<T>)?.contains(value)
        }

        @Override
        void describeTo(Description description) {
            description.appendText("A collection containing the value '$value'")
        }
    }
}