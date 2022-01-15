/**
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

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}