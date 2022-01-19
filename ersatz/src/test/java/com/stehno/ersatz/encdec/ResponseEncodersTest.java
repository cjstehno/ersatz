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
package com.stehno.ersatz.encdec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.function.Function;

import static com.stehno.ersatz.cfg.ContentType.IMAGE_GIF;
import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResponseEncodersTest {

    private static final Function<Object, byte[]> ENCODER_A = o -> new byte[0];
    private static final Function<Object, byte[]> ENCODER_B = o -> new byte[0];

    @Test @DisplayName("encoders") void encoders() {
        final var encoders = ResponseEncoders.encoders(e -> {
            e.register("text/plain", String.class, ENCODER_A);
            e.register(IMAGE_GIF, InputStream.class, ENCODER_B);
        });

        assertEquals(ENCODER_A, encoders.findEncoder(TEXT_PLAIN, String.class));
        assertEquals(ENCODER_A, encoders.findEncoder("text/plain", String.class));
        assertNull(encoders.findEncoder("text/plain", File.class));
    }
}