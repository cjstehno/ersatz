/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.server.undertow;

import groovy.lang.IntRange;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Undertow server callback used to provide the delayed chunked content.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class ResponseChunker implements IoCallback {

    private final List<byte[]> chunks;
    public final IntRange delay;

    public ResponseChunker(final List<byte[]> chunks, final IntRange delay) {
        this.chunks = chunks;
        this.delay = delay;
    }

    @Override
    public void onComplete(final HttpServerExchange exchange, final Sender sender) {
        if (chunks != null && !chunks.isEmpty()) {
            rest();
            sender.send(ByteBuffer.wrap(chunks.remove(0)), this);
        }
    }

    private void rest() {
        if (delay.size() > 1) {
            sleep(ThreadLocalRandom.current().nextLong(delay.getFrom(), delay.getTo()));
        } else {
            sleep(ThreadLocalRandom.current().nextLong(delay.getFrom()));
        }
    }

    private static void sleep(final long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            // ignore
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
     * @param content    the content array to be chunked
     * @param chunks the number of chunks
     * @return a List&lt;String&gt; containing the chunk data
     */
    public static List<byte[]> prepareChunks(final byte[] content, final int chunks) {
        int chunklen = content.length / chunks;
        int remainder = content.length % chunks;

        final List<byte[]> chunked = new LinkedList<>();

        int index = 0;

        for (int n = 0; n < chunks; n++) {
            int extra = 0;
            if (remainder > 0) {
                extra = 1;
                --remainder;
            }

            int len = chunklen + extra;

            final byte[] chunk = new byte[len];
            System.arraycopy(content, index, chunk, 0, len);
            chunked.add(chunk);

            index += len;
        }

        return chunked;
    }
}