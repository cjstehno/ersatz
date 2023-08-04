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
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static io.github.cjstehno.ersatz.match.ErsatzMatchers.byteArrayLike;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErsatzMatchersTest {

    @ParameterizedTest(name = "[{index}] {0} matches {1} -> {2}")
    @CsvSource({
        "some bytes,some bytes,true",
        "some bytes,other bytes,false"
    })
    void alikeByteArray(final String pattern, final String value, final boolean matches) {
        assertEquals(matches, byteArrayLike(pattern.getBytes(UTF_8)).matches(value.getBytes(UTF_8)));
    }

    @Test @DisplayName("byte array matcher description")
    void byteArrayMatcherDescription() {
        val matcher = byteArrayLike("stuff".getBytes(UTF_8));

        val desc = new StringDescription();
        matcher.describeTo(desc);
        assertEquals("A byte array of length 5 having 115 as the first element and 102 as the last.", desc.toString());
    }

    @Test @DisplayName("function matcher")
    void functionMatcher() {
        val matcher = ErsatzMatchers.functionMatcher(x -> x instanceof String);

        assertTrue(matcher.matches("a string"));
        assertFalse(matcher.matches(new Object()));

        val desc = new StringDescription();
        matcher.describeTo(desc);
        assertEquals("A function that checks for matching.", desc.toString());
    }

    @Test @DisplayName("string iterable matcher")
    void stringIterableMatcher() {
        val matcher = ErsatzMatchers.stringIterableMatcher(List.of(
            startsWith("prefix-"),
            equalTo("foobar"),
            endsWith("-suffix")
        ));

        assertTrue(matcher.matches(List.of("prefix-alpha", "foobar", "bravo-suffix")));
        assertFalse(matcher.matches(List.of("prefix-alpha", "other", "bravo-suffix")));
    }
}