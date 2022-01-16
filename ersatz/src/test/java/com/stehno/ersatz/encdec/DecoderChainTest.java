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

import static com.stehno.ersatz.cfg.ContentType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DecoderChainTest {

    @Test @DisplayName("chain") void chain() {
        final var global = RequestDecoders.decoders(d -> {
            d.register(TEXT_PLAIN, (b, ctx) -> "alpha-global");
            d.register("text/alternate", (b, ctx) -> "bravo-global");
            d.register(APPLICATION_JAVASCRIPT, (b, ctx) -> "charlie-global");
            d.register(APPLICATION_XML, (b, ctx) -> "foxtrot-global");
        });

        final var shared = RequestDecoders.decoders(d -> {
            d.register(TEXT_PLAIN, (b, ctx) -> "alpha-shared");
            d.register(APPLICATION_JSON, (b, ctx) -> "delta-shared");
        });

        final var local = RequestDecoders.decoders(d -> {
            d.register("text/alternate", (b, ctx) -> "bravo-local");
            d.register("application/date", (b, ctx) -> "echo-local");
        });

        DecoderChain chain = new DecoderChain(global);
        chain.first(local);
        chain.second(shared);

        assertEquals(3, chain.size());

        assertEquals(local, chain.getAt(0));
        assertEquals(shared, chain.getAt(1));
        assertEquals(global, chain.getAt(2));

        assertEquals("bravo-local", chain.resolve("text/alternate").apply(null, null));
        assertEquals("echo-local", chain.resolve("application/date").apply(null, null));

        assertEquals("delta-shared", chain.resolve(APPLICATION_JSON).apply(null, null));
        assertEquals("alpha-shared", chain.resolve(TEXT_PLAIN).apply(null, null));

        assertEquals("foxtrot-global", chain.resolve(APPLICATION_XML).apply(null, null));
    }
}
