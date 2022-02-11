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
package io.github.cjstehno.ersatz.impl;


import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.encdec.DecoderChain;
import io.github.cjstehno.ersatz.encdec.Decoders;
import io.github.cjstehno.ersatz.encdec.RequestDecoders;
import io.github.cjstehno.ersatz.match.QueryParamMatcher;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.HEAD;
import static io.github.cjstehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RequestMatcherTest {

    @ParameterizedTest @DisplayName("method") @MethodSource("methodProvider")
    void method(final HttpMethod method, final boolean result) {
        assertEquals(result, RequestMatcher.method(equalTo(HEAD)).matches(new MockClientRequest(method)));
    }

    private static Stream<Arguments> methodProvider() {
        return Stream.of(
            arguments(HEAD, true),
            arguments(GET, false)
        );
    }

    @ParameterizedTest @DisplayName("path")
    @CsvSource({
        "/something,true",
        "/some,false"
    })
    void path(final String path, final boolean result) {
        assertEquals(result, pathMatching("/something").matches(new MockClientRequest(GET, path)));
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

    @ParameterizedTest @DisplayName("param") @MethodSource("paramProvider")
    void params(final MockClientRequest request, final boolean result) {
        assertEquals(
            result,
            RequestMatcher.param(
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

    @ParameterizedTest @DisplayName("body") @MethodSource("bodyProvider")
    void body(final MockClientRequest request, final boolean result) {
        RequestDecoders decoders = RequestDecoders.decoders(d -> {
            d.register(TEXT_PLAIN, Decoders.utf8String);
        });

        assertEquals(result, RequestMatcher.body(new DecoderChain(decoders), TEXT_PLAIN.getValue(), equalTo("text content")).matches(request));
    }

    private static Stream<Arguments> bodyProvider() {
        return Stream.of(
            arguments(new MockClientRequest(), false),
            arguments(new MockClientRequest("text content".getBytes()), true),
            arguments(new MockClientRequest("text other content".getBytes()), false)
        );
    }

    @ParameterizedTest @DisplayName("matcher") @MethodSource("matcherProvider")
    void matcher(final MockClientRequest request, final boolean result) {
        RequestMatcher matcher = RequestMatcher.matcher(
            allOf(
                hasProperty("method", equalTo(GET)),
                hasProperty("contentLength", greaterThan(10L))
            )
        );

        assertEquals(result, matcher.matches(request));
    }

    private static Stream<Arguments> matcherProvider() {
        final var secondClient = new MockClientRequest(GET);
        secondClient.setContentLength(100);

        return Stream.of(
            arguments(new MockClientRequest(), false),
            arguments(secondClient, true)
        );
    }
}
