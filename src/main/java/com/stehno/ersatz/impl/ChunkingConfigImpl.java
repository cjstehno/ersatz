/**
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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.ChunkingConfig;
import groovy.lang.IntRange;

import static java.lang.String.format;

@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.BeanMembersShouldSerialize"})
public class ChunkingConfigImpl implements ChunkingConfig {

    private int chunks = 2;
    private IntRange range = new IntRange(0, 0);

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
        range = new IntRange(value, value);
        return this;
    }

    /**
     * Used to specify the delay time between each chunk as a range of millisecond values. The actual delay value will
     * be determined at runtime as a random value within the specified range.
     *
     * @param delayRange the delay value range in milliseconds
     * @return a reference to the config
     */
    public ChunkingConfig delay(final IntRange delayRange) {
        range = delayRange;
        return this;
    }

    public int getChunks() {
        return chunks;
    }

    public IntRange getDelay() {
        return range;
    }

    @Override public String toString() {
        return format("ChunkingConfig(chunks:%d, delay:%s)", chunks, range);
    }
}
