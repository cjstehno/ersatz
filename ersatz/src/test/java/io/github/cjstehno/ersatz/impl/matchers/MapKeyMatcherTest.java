/**
 * Copyright (C) 2024 Christopher J. Stehno
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

import io.github.cjstehno.ersatz.server.ClientRequest;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import lombok.val;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapKeyMatcherTest {

    @Test void keyMatchers() {
        val request = new MockClientRequest(GET, "/testing")
            .header("food", "beef")
            .header("drink", "water");

        val fooMatcher = new MapKeyMatcher("Foo", startsWith("foo"), false, ClientRequest::getHeaders);
        assertTrue(fooMatcher.matches(request));

        var desc = new StringDescription();
        fooMatcher.describeTo(desc);
        assertEquals("Foo name is map containing [a string starting with \"foo\"->ANYTHING]", desc.toString());

        val barMatcher = new MapKeyMatcher("Bar", startsWith("bar"), false, ClientRequest::getHeaders);
        assertFalse(barMatcher.matches(request));

        desc = new StringDescription();
        barMatcher.describeTo(desc);
        assertEquals("Bar name is map containing [a string starting with \"bar\"->ANYTHING]", desc.toString());
    }

    @Test void negatedKeyMatchers() {
        val request = new MockClientRequest(GET, "/testing")
            .header("food", "beef")
            .header("drink", "water");

        val fooMatcher = new MapKeyMatcher("Foo", startsWith("foo"), true, ClientRequest::getHeaders);
        assertFalse(fooMatcher.matches(request));

        val barMatcher = new MapKeyMatcher("Bar", startsWith("bar"), true, ClientRequest::getHeaders);
        assertTrue(barMatcher.matches(request));

        val desc = new StringDescription();
        barMatcher.describeTo(desc);
        assertEquals("Bar name is not map containing [a string starting with \"bar\"->ANYTHING]", desc.toString());
    }
}