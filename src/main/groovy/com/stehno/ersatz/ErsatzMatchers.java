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

/**
 * Some reusable Hamcrest matchers useful in Ersatz expectations.
 */
public class ErsatzMatchers {

    // FIXME: see if these can be removed

    /**
     * The provided matcher must match at least one element of the target collection (as Iterable).
     *
     * @param matcher the matcher to be wrapped
     * @return the wrapping matcher
     */
    public static <T> Matcher<Iterable<T>> collectionContainsMatch(final Matcher<T> matcher) {
        return new CollectionContainsMatchMatcher<T>(matcher);
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

    private static class CollectionContainsMatchMatcher<T> extends BaseMatcher<Iterable<T>> {

        private final Matcher<T> matcher;

        CollectionContainsMatchMatcher(final Matcher<T> matcher) {
            this.matcher = matcher;
        }

        @Override
        public boolean matches(final Object item) {
            if( item instanceof  Iterable){
                final var iter = (Iterable<T>)item;
                for (final T t : iter) {
                    if( matcher.matches(t)){
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("A collection matching ");
            description.appendDescriptionOf(matcher);
        }
    }

    private static class ByteArrayMatcher extends BaseMatcher<byte[]> {
        // FIXME: this can be better

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