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

import static java.lang.System.arraycopy;
import static lombok.AccessLevel.PACKAGE;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Undertow server callback used to provide the delayed chunked content.
 */
@RequiredArgsConstructor(access = PACKAGE)
class ResponseChunker implements IoCallback {

    private final List<byte[]> chunks;
    public final int delay;

    @Override
    public void onComplete(final HttpServerExchange exchange, final Sender sender) {
        if (chunks != null && !chunks.isEmpty()) {
            rest();
            sender.send(ByteBuffer.wrap(chunks.remove(0)), this);
        }
    }

    private void rest() {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @Override
    public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
        exception.printStackTrace();
    }

    /**
     * Splits the provided string of content into the specified number of chunks. Any remaining characters will be spread out over the chunks to
     * keep the sizes as even as possible.
     *
     * @param content the content array to be chunked
     * @param chunks  the number of chunks
     * @return a List&lt;String&gt; containing the chunk data
     */
    @SuppressWarnings("ReassignedVariable")
    public static List<byte[]> prepareChunks(final byte[] content, final int chunks) {
        val chunkLen = content.length / chunks;
        int remainder = content.length % chunks;

        final List<byte[]> chunked = new LinkedList<>();

        int index = 0;

        for (int n = 0; n < chunks; n++) {
            int extra = 0;
            if (remainder > 0) {
                extra = 1;
                --remainder;
            }

            val len = chunkLen + extra;
            val chunk = new byte[len];
            arraycopy(content, index, chunk, 0, len);
            chunked.add(chunk);

            index += len;
        }

        return chunked;
    }
}