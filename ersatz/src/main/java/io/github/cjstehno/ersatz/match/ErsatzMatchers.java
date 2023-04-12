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

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;

import java.util.Collection;
import java.util.function.Function;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

/**
 * Some reusable Hamcrest matchers useful in Ersatz expectations.
 */
@NoArgsConstructor(access = PRIVATE)
public final class ErsatzMatchers {

    /**
     * Matcher that matches if the object is an Iterable of Strings whose elements match (in any order) the provided
     * matchers.
     *
     * @param matchers the element matchers
     * @return the wrapping matcher
     */
    public static Matcher<Iterable<? super String>> stringIterableMatcher(final Collection<Matcher<? super String>> matchers) {
        return new StringIterableMatcher(matchers);
    }

    /**
     * The provided value must be a byte array with the same length and same first and last element values.
     *
     * @param array the array
     * @return the resulting matcher
     */
    public static Matcher<byte[]> byteArrayLike(final byte[] array) {
        return new ByteArrayMatcher(array);
    }

    /**
     * The provided function is used to verify the match, wrapped in a Matcher.
     *
     * @param fun the function being wrapped (a true response implies a match)
     * @param <T> the type of object(s) being matched
     * @return the function wrapped in a matcher
     */
    public static <T> Matcher<T> functionMatcher(final Function<T, Boolean> fun) {
        return new FunctionMatcher<>(fun);
    }
}

@RequiredArgsConstructor(access = PACKAGE)
class FunctionMatcher<T> extends BaseMatcher<T> {

    private final Function<T, Boolean> function;

    @Override public boolean matches(Object actual) {
        return function.apply((T) actual);
    }

    @Override public void describeTo(Description description) {
        description.appendText("A function that checks for matching.");
    }
}

@RequiredArgsConstructor(access = PACKAGE)
class StringIterableMatcher extends BaseMatcher<Iterable<? super String>> {

    private final Collection<Matcher<? super String>> matchers;

    @Override public boolean matches(final Object item) {
        return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers).matches(item);
    }

    @Override public void describeTo(final Description description) {
        description.appendText("An Iterable<String> matching {");
        matchers.forEach(description::appendDescriptionOf);
        description.appendText("}");
    }
}

@RequiredArgsConstructor(access = PACKAGE)
class ByteArrayMatcher extends BaseMatcher<byte[]> {

    private final byte[] array;

    @Override
    public boolean matches(final Object item) {
        final byte[] bytes = (byte[]) item;
        return bytes.length == array.length && bytes[0] == array[0] && bytes[bytes.length - 1] == array[array.length - 1];
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A byte array of length " + array.length + " having " + array[0] + " as the first element and " + array[array.length - 1] + " as the last.");
    }
}