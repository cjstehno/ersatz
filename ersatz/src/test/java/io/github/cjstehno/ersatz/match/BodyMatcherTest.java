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

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.github.cjstehno.ersatz.encdec.DecoderChain;
import io.github.cjstehno.ersatz.encdec.Decoders;
import io.github.cjstehno.ersatz.encdec.RequestDecoders;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BodyMatcherTest {

    @ParameterizedTest @DisplayName("body") @MethodSource("bodyProvider")
    void body(final MockClientRequest request, final boolean result) {
        RequestDecoders decoders = RequestDecoders.decoders(d -> {
            d.register(TEXT_PLAIN, Decoders.utf8String);
        });

        val bodyMatcher = BodyMatcher.bodyMatching(equalTo("text content"), TEXT_PLAIN.getValue());
        bodyMatcher.setDecoderChain(new DecoderChain(decoders, null));

        assertEquals(result, bodyMatcher.matches(request));
    }

    private static Stream<Arguments> bodyProvider() {
        return Stream.of(
            arguments(new MockClientRequest(), false),
            arguments(new MockClientRequest("text content".getBytes(), "text/plain"), true),
            arguments(new MockClientRequest("text other content".getBytes(), "text/plain"), false)
        );
    }
}