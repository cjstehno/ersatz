/*
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.encdec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EncodersTest {

    @ParameterizedTest @MethodSource("textProvider")
    @DisplayName("text data") void textData(final Object data, final String text) {
        assertArrayEquals(text.getBytes(), Encoders.text.apply(data));
    }

    private static Stream<Arguments> textProvider() {
        return Stream.of(
            arguments(null, ""),
            arguments("", ""),
            arguments("something interesting", "something interesting"),
            arguments(42, "42")
        );
    }

    @ParameterizedTest @MethodSource("base64Provider")
    @DisplayName("binary base64 data") void base64Data(final Object data, final String text) {
        assertArrayEquals(text.getBytes(), Encoders.binaryBase64.apply(data));
    }

    private static Stream<Arguments> base64Provider() {
        return Stream.of(
            arguments(null, ""),
            arguments(new byte[0], ""),
            arguments("some bytes".getBytes(), "c29tZSBieXRlcw=="),
            arguments(new ByteArrayInputStream("more bytes".getBytes()), "bW9yZSBieXRlcw==")
        );
    }
}