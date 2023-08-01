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

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static io.github.cjstehno.ersatz.match.QueryParamMatcher.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.github.cjstehno.ersatz.server.ClientRequest;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class QueryParamMatcherTest {

    private ClientRequest request;

    @BeforeEach void beforeEach() {
        request = new MockClientRequest(GET, "/testing")
            .query("alpha", "one", "two")
            .query("bravo", "two", "three")
            .query("charlie");
    }

    @Test @DisplayName("matching query param (matcher, matcher)")
    void queryMatchingWithMatcherMatcher() {
        assertTrue(queryMatching(equalTo("alpha"), hasItem("one")).matches(request));
        assertTrue(queryMatching(equalTo("alpha"), hasItem("two")).matches(request));
        assertTrue(queryMatching(equalTo("bravo"), hasItem("three")).matches(request));
        assertFalse(queryMatching(equalTo("alpha"), hasItem("four")).matches(request));
        assertFalse(queryMatching(equalTo("charlie"), hasItem("four")).matches(request));
    }

    @Test @DisplayName("matching query param (string, matcher)")
    void queryMatchingWithStringMatcher() {
        assertTrue(queryMatching("alpha", hasItem("one")).matches(request));
        assertTrue(queryMatching("alpha", hasItem("two")).matches(request));
        assertTrue(queryMatching("bravo", hasItem("three")).matches(request));
        assertFalse(queryMatching("alpha", hasItem("four")).matches(request));
        assertFalse(queryMatching("charlie", hasItem("four")).matches(request));
    }

    @Test @DisplayName("matching query exists")
    void matchingQueryExists() {
        assertTrue(queryExists("alpha").matches(request));
        assertTrue(queryExists("bravo").matches(request));
        assertTrue(queryExists("charlie").matches(request));
        assertFalse(queryExists("delta").matches(request));
    }

    @Test @DisplayName("matching query does not exist")
    void matchingQueryDoesNotExist() {
        assertFalse(queryDoesNotExist("alpha").matches(request));
        assertFalse(queryDoesNotExist("bravo").matches(request));
        assertFalse(queryDoesNotExist("charlie").matches(request));
        assertTrue(queryDoesNotExist("delta").matches(request));
    }

    @Test @DisplayName("matching query has param matching")
    void matchingHasParamMatcher() {
        assertTrue(queryHasParamMatching(startsWith("al")).matches(request));
        assertTrue(queryHasParamMatching(endsWith("ie")).matches(request));
        assertFalse(queryHasParamMatching(startsWith("xx")).matches(request));
    }

    @Test @DisplayName("matching query has given param and value")
    void matchingParamAndValue() {
        assertTrue(queryMatching("alpha", "one").matches(request));
        assertTrue(queryMatching("alpha", "two").matches(request));
        assertTrue(queryMatching("bravo", "three").matches(request));
        assertFalse(queryMatching("charlie", (String) null).matches(request));
        assertFalse(queryMatching("delta", "two").matches(request));
    }

    @ParameterizedTest @DisplayName("query") @MethodSource("queryProvider")
    void query(final MockClientRequest request, final boolean result) {
        assertEquals(
            result,
            QueryParamMatcher.queryMatching(
                "name",
                stringIterableMatcher(List.of(equalTo("alpha"), equalTo("blah")))
            ).matches(request)
        );
    }

    private static Stream<Arguments> queryProvider() {
        return Stream.of(
            arguments(new MockClientRequest().query("name", "alpha", "blah"), true),
            arguments(new MockClientRequest().query("name", "alpha"), false),
            arguments(new MockClientRequest(), false)
        );
    }
}