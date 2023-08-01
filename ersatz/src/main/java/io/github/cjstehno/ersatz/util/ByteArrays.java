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

import static java.lang.System.arraycopy;
import static lombok.AccessLevel.PRIVATE;

import java.nio.ByteBuffer;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 * Utilities for working with arrays of <code>byte</code>s.
 */
@NoArgsConstructor(access = PRIVATE)
public final class ByteArrays {

    /**
     * Joins all the byte arrays in the list, in order.
     *
     * @param arrays the list of byte arrays
     * @return a joined list of byte arrays
     */
    public static byte[] join(final List<byte[]> arrays) {
        byte[] current = new byte[0];

        for (final byte[] array : arrays) {
            current = join(current, array);
        }

        return current;
    }

    /**
     * Joins the two byte arrays into a single byte array, as first, then second.
     *
     * @param first the first byte array
     * @param second the second byte array
     * @return a merged byte array of the two in order
     */
    public static byte[] join(final byte[] first, final byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        arraycopy(first, 0, combined, 0, first.length);
        arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

    /**
     * Used to join the array of <code>ByteBuffer</code>s into a single array of bytes.
     *
     * @param buffers the ByteBuffers to be joined
     * @return a byte array container the merged bytes from the buffers
     */
    public static byte[] join(final ByteBuffer[] buffers) {
        byte[] incoming = new byte[0];

        for (final ByteBuffer b : buffers) {
            final byte[] data = new byte[b.remaining()];
            b.get(data);
            incoming = join(incoming, data);
        }

        return incoming;
    }
}
