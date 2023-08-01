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
package io.github.cjstehno.ersatz.cfg;

/**
 * Configuration for a chunked response. The response content-type will be specified as "chunked" and the response itself
 * will be written as partial chunks using the <code>chunks</code> property to determine the number of chunks, with the
 * <code>delay</code> property used to determine the delay between each chunk.
 */
public interface ChunkingConfig {

    /**
     * Used to specify the number of chunks which the response will be broken into,
     *
     * @param value the number of chunks
     * @return a reference to the config
     */
    ChunkingConfig chunks(final int value);

    /**
     * Used to specify the delay time between each chunk, fixed to the specified milliseconds.
     *
     * @param value the delay value in milliseconds
     * @return a reference to the config
     */
    ChunkingConfig delay(final int value);
}
