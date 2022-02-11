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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.match.HeaderMatcher.headerMatching;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class HeaderMatcherTest {

    @ParameterizedTest @DisplayName("header") @MethodSource("headerProvider")
    void header(final MockClientRequest request, final boolean result) {
        assertEquals(result, headerMatching("foo", hasItem("bar")).matches(request));
    }

    private static Stream<Arguments> headerProvider() {
        return Stream.of(
            arguments(new MockClientRequest().header("foo", "bar"), true),
            arguments(new MockClientRequest().header("one", "two"), false),
            arguments(new MockClientRequest().header("Foo", "bar"), true),
            arguments(new MockClientRequest().header("Foo", "Bar"), false),
            arguments(new MockClientRequest(), false)
        );
    }

    @ParameterizedTest @DisplayName("content-type") @MethodSource("contentTypeProvider")
    void contentType(final MockClientRequest request, final boolean result) {
        // FIXME: test it
//        assertEquals(result, contentTypeHeader(startsWith("application/")).matches(request));
    }

    private static Stream<Arguments> contentTypeProvider() {
        final var factory = new Function<String, MockClientRequest>() {
            @Override public MockClientRequest apply(String ctype) {
                final var mcr = new MockClientRequest();
                mcr.setContentType(ctype);
                return mcr;
            }
        };

        return Stream.of(
            arguments(factory.apply("application/json"), true),
            arguments(factory.apply("application/"), true),
            arguments(new MockClientRequest(), false)
        );
    }
}