/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;

import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;

/**
 * Some reusable Hamcrest matchers useful in Ersatz expectations.
 */
public class ErsatzMatchers {

    public static Matcher<String> pathMatcher(final String path) {
        return path.equals("*") ? Matchers.any(String.class) : equalTo(path);
    }

    public static Matcher<Iterable<? super String>> stringIterableMatcher(final Collection<Matcher<? super String>> matchers) {
        return new StringIterableMatcher(matchers);
    }

    private static class StringIterableMatcher extends BaseMatcher<Iterable<? super String>> {

        private final Collection<Matcher<? super String>> matchers;

        StringIterableMatcher(final Collection<Matcher<? super String>> matchers) {
            this.matchers = matchers;
        }

        @Override public boolean matches(final Object item) {
            return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers).matches(item);
        }

        @Override public void describeTo(final Description description) {
            description.appendText("An Iterable<String> matching {");
            matchers.forEach(description::appendDescriptionOf);
            description.appendText("}");
        }
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

    private static class ByteArrayMatcher extends BaseMatcher<byte[]> {

        private final byte[] array;

        ByteArrayMatcher(final byte[] array) {
            this.array = array;
        }

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
}