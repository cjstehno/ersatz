/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.ChunkingConfig;

import static java.lang.String.format;

/**
 * Default implementation of the <code>ChunkingConfig</code> interface.
 */
public class ChunkingConfigImpl implements ChunkingConfig {

    private int chunks = 2;
    private int delay;

    /**
     * Used to specify the number of chunks which the response will be broken into,
     *
     * @param value the number of chunks
     * @return a reference to the config
     */
    public ChunkingConfig chunks(final int value) {
        chunks = value;
        return this;
    }

    /**
     * Used to specify the delay time between each chunk, fixed to the specified milliseconds.
     *
     * @param value the delay value in milliseconds
     * @return a reference to the config
     */
    public ChunkingConfig delay(final int value) {
        this.delay = value;
        return this;
    }

    /**
     * Used to retrieve the number of chunks (defaults to 2).
     *
     * @return the number of chunks
     */
    public int getChunks() {
        return chunks;
    }

    /**
     * Used to retrieve the chunk delay.
     *
     * @return the chunk delay.
     */
    public int getDelay() {
        return delay;
    }

    @Override public String toString() {
        return format("ChunkingConfig(chunks:%d, delay:%s)", chunks, delay);
    }
}
