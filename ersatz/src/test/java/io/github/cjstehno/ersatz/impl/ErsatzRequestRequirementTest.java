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
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.ANY;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.match.CookieMatcher.cookieMatcher;
import static io.github.cjstehno.ersatz.match.HttpMethodMatcher.methodMatching;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErsatzRequestRequirementTest {

    @Test void requiresSecure() {
        val request = new MockClientRequest(GET, "/test").scheme("HTTPS");

        var requirement = (ErsatzRequestRequirement) anyRequest().secure(true);
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().secure();
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().secure(false);
        assertTrue(requirement.matches(request));
        assertFalse(requirement.check(request));
    }

    @Test void requiresHeader() {
        val request = new MockClientRequest(GET, "/test").header("Content-Disposition", "file");

        var requirement = (ErsatzRequestRequirement) anyRequest().header("Content-Disposition", "file");
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().header("Content-Disposition", IsIterableContaining.hasItem("file"));
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().header("Content-Disposition", "stream");
        assertTrue(requirement.matches(request));
        assertFalse(requirement.check(request));
    }

    @Test void requiresQuery() {
        val request = new MockClientRequest(GET, "/test").query("name", "Bob");

        var requirement = (ErsatzRequestRequirement) anyRequest().query("name", "Bob");
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().query("name");
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().query("name", List.of("Bob"));
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().query("name", IsIterableContaining.hasItem("Bob"));
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().header("name", "Chris");
        assertTrue(requirement.matches(request));
        assertFalse(requirement.check(request));
    }

    @Test void requiresCookie() {
        val request = new MockClientRequest(GET, "/test").cookie("flavor", "chocolate-chip");

        var requirement = (ErsatzRequestRequirement) anyRequest().cookie("flavor", "chocolate-chip");
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().cookie("flavor", cookieMatcher(c -> {
            c.value("chocolate-chip");
        }));
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().cookie("flavor", "sugar");
        assertTrue(requirement.matches(request));
        assertFalse(requirement.check(request));
    }

    @Test void requiresMatcher() {
        val request = new MockClientRequest(GET, "/test").query("name", "Bob");

        var requirement = (ErsatzRequestRequirement) anyRequest().matcher(allOf(
            hasProperty("queryParams", not(anEmptyMap()))
        ));
        assertTrue(requirement.matches(request));
        assertTrue(requirement.check(request));

        requirement = (ErsatzRequestRequirement) anyRequest().matcher(allOf(
            hasProperty("queryParams", anEmptyMap())
        ));
        assertTrue(requirement.matches(request));
        assertFalse(requirement.check(request));
    }

    private static ErsatzRequestRequirement anyRequest() {
        return new ErsatzRequestRequirement(methodMatching(ANY), pathMatching(any(String.class)));
    }
}