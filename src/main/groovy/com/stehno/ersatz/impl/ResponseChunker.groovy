/*
 * Copyright (C) 2018 Christopher J. Stehno
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
package com.stehno.ersatz.impl

import groovy.transform.TupleConstructor
import io.undertow.io.IoCallback
import io.undertow.io.Sender
import io.undertow.server.HttpServerExchange

import java.util.concurrent.ThreadLocalRandom

/**
 * Undertow server callback used to provide the delayed chunked content.
 */
@TupleConstructor
class ResponseChunker implements IoCallback {

    final List<String> chunks
    final IntRange delay

    @Override
    void onComplete(HttpServerExchange exchange, Sender sender) {
        if (chunks) {
            rest()
            sender.send(chunks.remove(0), this)
        }
    }

    private void rest() {
        if (delay.size() > 1) {
            sleep ThreadLocalRandom.current().nextLong(delay.from, delay.to)
        } else {
            sleep ThreadLocalRandom.current().nextLong(delay.from)
        }
    }

    @Override
    void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
        exception.printStackTrace()
    }

    /**
     * Splits the provided string of content into the specified number of chunks. Any remaining characters will be spread out over the chunks to
     * keep the sizes as even as possible.
     *
     * @param str the string to be chunked
     * @param chunks the number of chunks
     * @return a List<String> containing the chunk data
     */
    static List<String> prepareChunks(final String str, final int chunks) {
        int chunklen = str.length() / chunks
        int remainder = str.length() % chunks

        List<String> chunked = []

        int index = 0
        chunks.times { n ->
            int extra = 0
            if (remainder) {
                extra = 1
                --remainder
            }

            int len = chunklen + extra
            chunked << str[index..(index + len - 1)]
            index += len
        }

        chunked
    }
}