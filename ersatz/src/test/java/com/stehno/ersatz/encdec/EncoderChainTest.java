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
import org.junit.jupiter.api.Test;

import static com.stehno.ersatz.cfg.ContentType.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EncoderChainTest {

    public static final byte[] ALPHA_GLOBAL = "alpha-global".getBytes(UTF_8);
    public static final byte[] BRAVO_SHARED = "bravo-shared".getBytes(UTF_8);
    public static final byte[] DELTA_SHARED = "delta-shared".getBytes(UTF_8);
    public static final byte[] ECHO_LOCAL = "echo-local".getBytes(UTF_8);
    public static final byte[] CHARLIE_LOCAL = "charlie-local".getBytes(UTF_8);
    public static final byte[] BRAVO_GLOBAL = "bravo-global".getBytes(UTF_8);
    public static final byte[] CHARLIE_GLOBAL = "charlie-global".getBytes(UTF_8);

    @Test @DisplayName("chain") void chain() {
        final var global = ResponseEncoders.encoders(e -> {
            e.register(TEXT_PLAIN, String.class, o -> ALPHA_GLOBAL);
            e.register(APPLICATION_XML, String.class, o -> BRAVO_GLOBAL);
            e.register(APPLICATION_JSON, String.class, o -> CHARLIE_GLOBAL);
        });

        final var shared = ResponseEncoders.encoders(e -> {
            e.register(APPLICATION_XML, String.class, o -> BRAVO_SHARED);
            e.register(APPLICATION_JAVASCRIPT, String.class, o -> DELTA_SHARED);
        });

        final var local = ResponseEncoders.encoders(e -> {
            e.register(APPLICATION_JSON, String.class, o -> CHARLIE_LOCAL);
            e.register(APPLICATION_URLENCODED, String.class, o -> ECHO_LOCAL);
        });

        EncoderChain chain = new EncoderChain(global);
        chain.first(local);
        chain.second(shared);

        assertArrayEquals(CHARLIE_LOCAL, chain.resolve(APPLICATION_JSON, String.class).apply(null));
        assertArrayEquals(ECHO_LOCAL, chain.resolve(APPLICATION_URLENCODED, String.class).apply(null));
        assertArrayEquals(DELTA_SHARED, chain.resolve(APPLICATION_JAVASCRIPT, String.class).apply(null));
        assertArrayEquals(BRAVO_SHARED, chain.resolve(APPLICATION_XML, String.class).apply(null));
        assertArrayEquals(ALPHA_GLOBAL, chain.resolve(TEXT_PLAIN, String.class).apply(null));
    }
}
