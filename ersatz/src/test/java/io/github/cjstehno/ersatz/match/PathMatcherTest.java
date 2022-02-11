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
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.server.MockClientRequest;
import lombok.val;
import org.hamcrest.StringDescription;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PathMatcherTest {

    @ParameterizedTest(name = "[{index}] {0} matches {1} -> {2}")
    @CsvSource({
        "*,/alpha/bravo,true",
        "*,/other,true",
        "/foo,/foo,true",
        "/foo,/bar,false",
        "/foo,/foo/bar,false"
    })
    void matchingPaths(final String pattern, final String path, final boolean matches) {
        assertEquals(matches, pathMatching(pattern).matches(new MockClientRequest(GET, path)));
    }

    @ParameterizedTest(name = "[{index}] {0} matches {1} -> {2}")
    @CsvSource({
        "/foo,/foo,true",
        "/foo,/bar,false",
        "/foo,/foo/bar,true"
    })
    void matchingPathsWithMatcher(final String pattern, final String path, final boolean matches) {
        assertEquals(matches, pathMatching(startsWith(pattern)).matches(new MockClientRequest(GET, path)));
    }

    @ParameterizedTest(name = "[{index}] {0} matches {1} -> {2}")
    @CsvSource({
        "/foo,/foo,true",
        "/foo,/bar,false",
        "/foo,/foo/bar,true"
    })
    void matchingPathsWithPredicate(final String pattern, final String path, final boolean matches) {
        assertEquals(matches, pathMatching(p -> p.startsWith(pattern)).matches(new MockClientRequest(GET, path)));
    }

    @ParameterizedTest(name = "[{index}] {0} matches {1} -> {2}")
    @CsvSource({
        "/foo,/foo,true",
        "/foo,/bar,false",
        "/foo,/foo/bar,true"
    })
    void matchingPathsWithPredicateAndDescription(final String pattern, final String path, final boolean matches) {
        val matcher = pathMatching(
            "path starting with " + pattern,
            p -> p.startsWith(pattern)
        );

        assertEquals(matches, matcher.matches(new MockClientRequest(GET, path)));

        val desc = new StringDescription();
        matcher.describeTo(desc);
        assertEquals("Path matches path starting with /foo", desc.toString());
    }
}