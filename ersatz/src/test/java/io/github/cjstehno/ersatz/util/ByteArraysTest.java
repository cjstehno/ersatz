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
package io.github.cjstehno.ersatz.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteArraysTest {

    private static final byte[] A_0 = "first byte array".getBytes();
    private static final byte[] A_1 = "second byte array".getBytes();
    private static final byte[] A_2 = "third byte array".getBytes();

    @Test @DisplayName("joining two byte arrays")
    void joiningTwo() {
        final var result = ByteArrays.join(A_0, A_1);
        assertEquals("first byte arraysecond byte array", new String(result));
    }

    @Test @DisplayName("joining a collection of arrays")
    void joiningCollection() {
        final var result = ByteArrays.join(List.of(A_0, A_1, A_2));
        assertEquals("first byte arraysecond byte arraythird byte array", new String(result));
    }

    @Test @DisplayName("joining an array of bytebuffers")
    void joiingBuffers() {
        final var result = ByteArrays.join(new ByteBuffer[]{ByteBuffer.wrap(A_0), ByteBuffer.wrap(A_2)});
        assertEquals("first byte arraythird byte array", new String(result));
    }
}