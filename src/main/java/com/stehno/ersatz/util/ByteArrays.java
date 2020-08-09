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
package com.stehno.ersatz.util;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.System.arraycopy;

public class ByteArrays {

    public static byte[] join(final List<byte[]> arrays) {
        byte[] current = new byte[0];

        for (final byte[] array : arrays) {
            current = join(current, array);
        }

        return current;
    }

    public static byte[] join(final byte[] first, final byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        arraycopy(first, 0, combined, 0, first.length);
        arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

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
