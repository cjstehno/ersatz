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

import static io.github.cjstehno.ersatz.cfg.HttpMethod.POST;
import static io.github.cjstehno.ersatz.match.BodyParamMatcher.*;
import static io.github.cjstehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.github.cjstehno.ersatz.server.MockClientRequest;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BodyParamMatcherTest {

    @ParameterizedTest @DisplayName("param matching") @MethodSource("paramProvider")
    void params(final MockClientRequest request, final boolean result) {
        assertEquals(
            result,
            bodyParamMatching(
                "email",
                stringIterableMatcher(List.of(containsString("@goomail.com")))
            ).matches(request)
        );
    }

    private static Stream<Arguments> paramProvider() {
        final var factory = new Function<Map<String, Deque<String>>, MockClientRequest>() {
            @Override public MockClientRequest apply(Map<String, Deque<String>> map) {
                final var mcr = new MockClientRequest();
                mcr.setBodyParameters(map);
                return mcr;
            }
        };

        return Stream.of(
            arguments(new MockClientRequest(), false),
            arguments(factory.apply(Map.of(
                "email", new ArrayDeque<>(List.of("foo@goomail.com")),
                "spam", new ArrayDeque<>(List.of("n"))
            )), true),
            arguments(factory.apply(Map.of(
                "spam", new ArrayDeque<>(List.of("n"))
            )), false)
        );
    }

    @Test @DisplayName("body param exists")
    void bodyParamExisting() {
        val request = new MockClientRequest(POST, "/testing")
            .param("alpha", "one")
            .param("bravo", "two", "three");

        assertTrue(bodyParamExists("alpha").matches(request));
        assertTrue(bodyParamExists("bravo").matches(request));
        assertFalse(bodyParamExists("charlie").matches(request));

        assertTrue(bodyParamExists(startsWith("alp")).matches(request));
        assertTrue(bodyParamExists(endsWith("avo")).matches(request));
        assertFalse(bodyParamExists(startsWith("char")).matches(request));
    }

    @Test @DisplayName("body param does not exist")
    void bodyParamNotExisting() {
        val request = new MockClientRequest(POST, "/testing")
            .param("alpha", "one")
            .param("bravo", "two", "three");

        assertFalse(bodyParamDoesNotExist("alpha").matches(request));
        assertFalse(bodyParamDoesNotExist("bravo").matches(request));
        assertTrue(bodyParamDoesNotExist("charlie").matches(request));

        assertFalse(bodyParamDoesNotExist(startsWith("alp")).matches(request));
        assertFalse(bodyParamDoesNotExist(endsWith("avo")).matches(request));
        assertTrue(bodyParamDoesNotExist(startsWith("char")).matches(request));
    }
}