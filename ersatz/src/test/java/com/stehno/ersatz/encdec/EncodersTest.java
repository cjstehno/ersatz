/**
 * Copyright (C) 2022 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.encdec;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.stehno.ersatz.TestHelpers.*;
import static com.stehno.ersatz.cfg.ContentType.IMAGE_GIF;
import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncodersTest {

    private static final Function<Object, byte[]> ENCODER_A = o -> new byte[0];
    private static final Function<Object, byte[]> ENCODER_B = o -> new byte[0];

    @ParameterizedTest @DisplayName("text data") @MethodSource("textProvider")
    void textData(final Object data, final String text) {
        assertArrayEquals(text.getBytes(), Encoders.text.apply(data));
    }

    @ParameterizedTest @DisplayName("text data with charset (String)") @MethodSource("textProvider")
    void textDataWithCharsetString(final Object data, final String text) {
        assertArrayEquals(text.getBytes(UTF_8), Encoders.text("UTF-8").apply(data));
    }

    @ParameterizedTest @DisplayName("text data with charset (Charset)") @MethodSource("textProvider")
    void textDataWithCharset(final Object data, final String text) {
        assertArrayEquals(text.getBytes(UTF_8), Encoders.text(UTF_8).apply(data));
    }

    @ParameterizedTest @MethodSource("base64Provider")
    @DisplayName("binary base64 data") void base64Data(final Object data, final String text) {
        assertArrayEquals(text.getBytes(), Encoders.binaryBase64.apply(data));
    }

    @Test @DisplayName("binaryBase64 with InputStream error")
    void base64InputStreamError() throws IOException {
        val stream = mock(InputStream.class);
        when(stream.readAllBytes()).thenThrow(new IOException("doh!"));

        val thrown = assertThrows(IllegalArgumentException.class, () -> {
            Encoders.binaryBase64.apply(stream);
        });
        assertEquals("Unable to read InputStream: doh!", thrown.getMessage());
    }

    @ParameterizedTest(name = "[{index}] content encoder: {0}")
    @MethodSource("contentProvider")
    void content(final Object object) {
        assertArrayEquals("This is some file content.".getBytes(), Encoders.content.apply(object));
    }

    @Test @DisplayName("remote url content")
    void remoteUrlContent() throws MalformedURLException {
        // Note: if this test fails, make sure the url is still valid before digging too deep.
        val bytes = Encoders.content.apply(new URL("http://cjstehno.github.io/index.html"));
        assertTrue(bytes.length > 1000);
    }

    @Test @DisplayName("encoders") void encoders() {
        final var encoders = ResponseEncoders.encoders(e -> {
            e.register("text/plain", String.class, ENCODER_A);
            e.register(IMAGE_GIF, InputStream.class, ENCODER_B);
        });

        assertEquals(ENCODER_A, encoders.findEncoder(TEXT_PLAIN, String.class));
        assertEquals(ENCODER_A, encoders.findEncoder("text/plain", String.class));
        assertNull(encoders.findEncoder("text/plain", File.class));
    }

    @Test @DisplayName("inputStream error")
    void inputStreamWithError() throws IOException {
        val stream = mock(InputStream.class);
        when(stream.readAllBytes()).thenThrow(new IOException("oops"));

        val thrown = assertThrows(IllegalArgumentException.class, () -> {
            Encoders.content.apply(stream);
        });
        assertEquals("Unable to resolve content due to error: oops", thrown.getMessage());
    }

    @Test @DisplayName("multipart with invalid object")
    void invalidMultipart(){
        val thrown = assertThrows(IllegalArgumentException.class, ()-> {
            Encoders.multipart.apply(new Object());
        });
        assertEquals("java.lang.Object found, MultipartRequestContent is required.", thrown.getMessage());
    }

    private static Stream<Arguments> textProvider() {
        return Stream.of(
            arguments(null, ""),
            arguments("", ""),
            arguments("something interesting", "something interesting"),
            arguments(42, "42")
        );
    }

    private static Stream<Arguments> contentProvider() throws URISyntaxException {
        return Stream.of(
            arguments(resourcePath("/content.txt")),
            arguments("/content.txt"),
            arguments(resourceFile("/content.txt")),
            arguments(resourceUrl("/content.txt")),
            arguments(resourceUri("/content.txt")),
            arguments(resourceStream("/content.txt"))
        );
    }

    private static Stream<Arguments> base64Provider() {
        return Stream.of(
            arguments(null, ""),
            arguments(new byte[0], ""),
            arguments("some bytes".getBytes(), "c29tZSBieXRlcw=="),
            arguments("raw string bytes", "cmF3IHN0cmluZyBieXRlcw=="),
            arguments(new ByteArrayInputStream("more bytes".getBytes()), "bW9yZSBieXRlcw==")
        );
    }
}
