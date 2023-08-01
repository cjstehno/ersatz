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

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cjstehno.ersatz.match.PathMatcher;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import lombok.val;
import org.junit.jupiter.api.Test;

class MatchCountingMatcherTest {

    @Test void matching() {
        val matcher = PathMatcher.pathMatching("/foo");
        val counter = MatchCountingMatcher.countingMatcher(matcher);

        assertEquals(0, counter.getMatchedCount());

        assertTrue(counter.matches(new MockClientRequest(GET, "/foo")));
        assertEquals(1, counter.getMatchedCount());

        assertFalse(counter.matches(new MockClientRequest(GET, "/bar")));
        assertEquals(1, counter.getMatchedCount());
    }
}