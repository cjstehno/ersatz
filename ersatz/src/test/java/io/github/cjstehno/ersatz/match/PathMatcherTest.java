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
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.server.ClientRequest;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.match.PathMatcher.anyPath;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(matches, pathMatching(pattern).matches(mockRequest(path)));
    }

    @ParameterizedTest(name = "[{index}] {0} matches {1} -> {2}")
    @CsvSource({
            "/foo,/foo,true",
            "/foo,/bar,false",
            "/foo,/foo/bar,true"
    })
    void matchingPathsWithMatcher(final String pattern, final String path, final boolean matches) {
        assertEquals(matches, pathMatching(startsWith(pattern)).matches(mockRequest(path)));
    }

    @ParameterizedTest
    @CsvSource({"/foo", "/bar", "/baz/bing"})
    void matchingAnyPath(final String path) {
        assertTrue(anyPath().matches(mockRequest(path)));
    }

    private static ClientRequest mockRequest(final String path){
        return new MockClientRequest(GET, path);
    }
}