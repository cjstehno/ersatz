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
package io.github.cjstehno.ersatz.impl.matchers;

import lombok.val;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PredicateMatcherTest {

    @ParameterizedTest @CsvSource({
        "5,false",
        "10,false",
        "15,true"
    })
    void withoutDescription(final int value , final boolean expects){
        val matcher = new PredicateMatcher<Integer>(num -> num > 10);

        assertEquals(expects, matcher.matches(value));

        val desc = new StringDescription();
        matcher.describeTo(desc);
        assertEquals("a configured predicate", desc.toString());
    }

    @ParameterizedTest @CsvSource({
        "5,false",
        "10,false",
        "15,true"
    })
    void withDescription(final int value , final boolean expects){
        val matcher = new PredicateMatcher<Integer>(
            num -> num > 10,
            "a number greater than 10"
        );

        assertEquals(expects, matcher.matches(value));

        val desc = new StringDescription();
        matcher.describeTo(desc);
        assertEquals("a number greater than 10", desc.toString());
    }
}