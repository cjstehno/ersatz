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
package io.github.cjstehno.ersatz.impl.matchers;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.PUT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.*;

import io.github.cjstehno.ersatz.server.ClientRequest;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import lombok.val;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

class MappedValuesMatcherTest {

    @Test void mappedValues() {
        val request = new MockClientRequest(PUT, "/testing")
            .query("alpha", "one")
            .query("bravo", "two", "three");

        var matcher = new MappedValuesMatcher("Query param", equalTo("alpha"), hasItem("one"), ClientRequest::getQueryParams);
        assertTrue(matcher.matches(request));

        matcher = new MappedValuesMatcher("Query param", equalTo("bravo"), hasItem("three"), ClientRequest::getQueryParams);
        assertTrue(matcher.matches(request));

        matcher = new MappedValuesMatcher("Query param", equalTo("bravo"), hasItem("four"), ClientRequest::getQueryParams);
        assertFalse(matcher.matches(request));

        val desc = new StringDescription();
        matcher.describeTo(desc);
        assertEquals("Query param name is \"bravo\" and values are a collection containing \"four\"", desc.toString());
    }
}