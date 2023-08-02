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
package io.github.cjstehno.ersatz.server.undertow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseChunkerTest {

    private static final byte[] BYTES = "abcdefghijklmnop".getBytes();

    @Test @DisplayName("parsing 3 chunks")
    void parsing3Chunks(){
        final var chunks = ResponseChunker.prepareChunks(BYTES, 3);

        assertEquals(3, chunks.size());
        assertEquals("abcdef", new String(chunks.get(0)));
        assertEquals("ghijk", new String(chunks.get(1)));
        assertEquals("lmnop", new String(chunks.get(2)));
    }

    @Test @DisplayName("parsing 2 chunks")
    void parsing2Chunks(){
        final var chunks = ResponseChunker.prepareChunks(BYTES, 2);

        assertEquals(2, chunks.size());
        assertEquals("abcdefgh", new String(chunks.get(0)));
        assertEquals("ijklmnop", new String(chunks.get(1)));
    }
}