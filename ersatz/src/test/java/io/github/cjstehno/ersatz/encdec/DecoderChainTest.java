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

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JAVASCRIPT;
import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON;
import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_XML;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DecoderChainTest {

    @Test @DisplayName("chain") void chain() {
        final var global = RequestDecoders.decoders(d -> {
            d.register(TEXT_PLAIN, (b, ctx) -> "alpha-global");
            d.register("text/alternate", (b, ctx) -> "bravo-global");
            d.register(APPLICATION_JAVASCRIPT, (b, ctx) -> "charlie-global");
            d.register(APPLICATION_XML, (b, ctx) -> "foxtrot-global");
        });

        final var local = RequestDecoders.decoders(d -> {
            d.register("text/alternate", (b, ctx) -> "bravo-local");
            d.register("application/date", (b, ctx) -> "echo-local");
        });

        val chain = new DecoderChain(global, local);

        assertEquals("bravo-local", chain.resolve("text/alternate").apply(null, null));
        assertEquals("echo-local", chain.resolve("application/date").apply(null, null));

        assertNull(chain.resolve(APPLICATION_JSON));
        assertEquals("alpha-global", chain.resolve(TEXT_PLAIN).apply(null, null));
        assertEquals("foxtrot-global", chain.resolve(APPLICATION_XML).apply(null, null));
    }

    @Test void decoderChainWithOverride() {
        val global = RequestDecoders.decoders(d -> {
            d.register(TEXT_PLAIN, Decoders.string());
        });

        val local = RequestDecoders.decoders(d -> {
            d.register(
                TEXT_PLAIN,
                (bytes, context) -> "local: " + context.getDecoderChain().resolveServerLevel(TEXT_PLAIN).apply(bytes, context)
            );
        });

        val chain = new DecoderChain(global, local);

        val content = "some content".getBytes(UTF_8);
        val decoded = chain.resolve(TEXT_PLAIN).apply(
            content,
            new DecodingContext(content.length, "text/plain", "UTF-8", chain)
        );

        assertEquals("local: some content", decoded);
    }
}
