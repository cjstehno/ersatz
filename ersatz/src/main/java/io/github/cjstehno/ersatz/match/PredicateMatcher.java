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

import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

/**
 * Matcher used to match based on the result of a Predicate.
 */
@RequiredArgsConstructor(access = PRIVATE)
public class PredicateMatcher<T> extends BaseMatcher<T> {

    private final String description;
    private final Predicate<T> predicate;

    /**
     * Creates a matcher based on the given predicate, with a generic description string. The matcher will match if the
     * predicate returns <code>true</code>.
     *
     * @param predicate the predicate
     * @param <T>       the type of object being tested
     * @return the predicate matcher
     */
    public static <T> PredicateMatcher<T> predicatedBy(final Predicate<T> predicate) {
        return new PredicateMatcher<>("a predicate function", predicate);
    }

    /**
     * Creates a matcher based on the given predicate, with the provided description. The matcher will match if the
     * predicate returns <code>true</code>.
     *
     * @param description the description used to describe the match requirements
     * @param predicate   the predicate
     * @param <T>         the type of object being tested
     * @return the predicate matcher
     */
    public static <T> PredicateMatcher<T> predicatedBy(final String description, final Predicate<T> predicate) {
        return new PredicateMatcher<>(description, predicate);
    }

    @Override @SuppressWarnings("unchecked")
    public boolean matches(final Object actual) {
        return predicate.test((T) actual);
    }

    @Override public void describeTo(final Description desc) {
        desc.appendText(description);
    }
}
