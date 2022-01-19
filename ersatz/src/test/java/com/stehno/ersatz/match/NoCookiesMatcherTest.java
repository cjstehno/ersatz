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
package com.stehno.ersatz.match;

import lombok.val;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NoCookiesMatcherTest {

    @Test void noCookies(){
        val matcher = NoCookiesMatcher.noCookies();

        assertFalse(matcher.matches("not a map"));
        assertTrue(matcher.matches(Map.of()));
        assertFalse(matcher.matches(Map.of("one", "two")));
    }

    @Test void description(){
        val desc = new StringDescription();
        NoCookiesMatcher.noCookies().describeTo(desc);
        assertEquals("Has no cookies.", desc.toString());
    }
}