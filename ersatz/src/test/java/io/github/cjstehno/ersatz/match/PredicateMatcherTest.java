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

import lombok.val;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PredicateMatcherTest {

    @ParameterizedTest @CsvSource({
        "5,false",
        "10,false",
        "15,true"
    })
    void withoutDescription(final int value, final boolean expects) {
        val matcher = PredicateMatcher.<Integer>predicatedBy(num -> num > 10);

        assertEquals(expects, matcher.matches(value));
        assertDescription("a predicate function", matcher);
    }

    @ParameterizedTest @CsvSource({
        "5,false",
        "10,false",
        "15,true"
    })
    void withDescription(final int value, final boolean expects) {
        val matcher = PredicateMatcher.<Integer>predicatedBy(
            "a number greater than 10",
            num -> num > 10
        );

        assertEquals(expects, matcher.matches(value));
        assertDescription("a number greater than 10", matcher);
    }

    private static void assertDescription(final String expected, final Matcher<?> matcher) {
        val desc = new StringDescription();
        matcher.describeTo(desc);
        assertEquals(expected, desc.toString());
    }
}