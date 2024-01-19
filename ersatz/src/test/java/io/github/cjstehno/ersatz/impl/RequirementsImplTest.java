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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.server.MockClientRequest;
import lombok.val;
import org.junit.jupiter.api.Test;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.POST;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementsImplTest {

    @Test
    void requireThat() {
        val requirements = new RequirementsImpl();
        requirements.that(POST, pathMatching(startsWith("/foo")), req -> {
            req.header("foo", "bar");
        });

        // this one matches and meets the requirement -> true
        assertTrue(requirements.check(new MockClientRequest(POST, "/foo").header("foo", "bar")));

        // this one matches but does not meet the requirement -> false
        assertFalse(requirements.check(new MockClientRequest(POST, "/foo")));

        // this one does not match -> true
        assertTrue(requirements.check(new MockClientRequest(POST, "/bar").header("foo", "bar")));
    }
}