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
package io.github.cjstehno.ersatz.encdec;

import io.github.cjstehno.ersatz.encdec.Decoders;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class DecodersTest {

    private static final String STRING = "this is a string!";
    private static final byte[] STRING_BYTES_UTF = STRING.getBytes(UTF_8);

    @Test void passthrough() {
        final var bytes = new byte[10];
        ThreadLocalRandom.current().nextBytes(bytes);

        assertArrayEquals(bytes, (byte[]) Decoders.passthrough.apply(bytes, null));
    }

    @Test void string() {
        assertEquals(STRING, Decoders.utf8String.apply(STRING_BYTES_UTF, null));
        assertEquals(STRING, Decoders.string().apply(STRING_BYTES_UTF, null));
        assertEquals(STRING, Decoders.string("UTF-8").apply(STRING_BYTES_UTF, null));
        assertEquals(STRING, Decoders.string(UTF_8).apply(STRING_BYTES_UTF, null));
    }

    @Test @DisplayName("urlEncoded") @SuppressWarnings("unchecked")
    void urlEncoded() {
        val bytes = "some+name=a+value&key=value".getBytes(UTF_8);
        val decoded = Decoders.urlEncoded.apply(bytes, null);

        assertTrue(decoded instanceof Map);

        val map = (Map<String, String>) decoded;
        assertEquals(2, map.size());
        assertEquals("a value", map.get("some name"));
        assertEquals("value", map.get("key"));
    }

    @Test @DisplayName("urlEncoded empty") @SuppressWarnings("unchecked")
    void urlEncodedEmpty() {
        val bytes = "".getBytes(UTF_8);
        val decoded = Decoders.urlEncoded.apply(bytes, null);

        assertTrue(decoded instanceof Map);

        val map = (Map<String, String>) decoded;
        assertTrue(map.isEmpty());
    }

    @Test @DisplayName("urlEncoded with error") @SuppressWarnings("unchecked")
    void urlEncodedError() {
        val thrown = assertThrows(IllegalArgumentException.class, () -> {
            Decoders.urlEncoded.apply("some%asdfname=a+value".getBytes(UTF_8), null);
        });
        assertEquals("URLDecoder: Illegal hex characters in escape (%) pattern - Error at index 1 in: \"as\"", thrown.getMessage());
    }

    @Test @DisplayName("multipart error")
    void multipartError() {
        val thrown = assertThrows(IllegalArgumentException.class, () -> {
            Decoders.multipart.apply(null, null);
        });
        assertEquals(
            "Cannot invoke \"io.github.cjstehno.ersatz.encdec.DecodingContext.getContentType()\" because \"this.ctx\" is null",
            thrown.getMessage()
        );
    }
}