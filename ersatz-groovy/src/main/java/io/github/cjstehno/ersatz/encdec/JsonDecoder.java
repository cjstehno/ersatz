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
package io.github.cjstehno.ersatz.encdec;

import groovy.json.JsonSlurper;

import java.util.function.BiFunction;

/**
 * Provides a Decoder for JSON content based on the Groovy <code>JsonSlurper</code>. This decoder will only handle
 * simple standard objects. If you need a more robust mapping of JSON to objects consider using the Jackson
 * JSON library.
 */
public class JsonDecoder implements BiFunction<byte[], DecodingContext, Object> {

    /**
     * A helper method to provide a new instance of a JsonDecoder.
     *
     * @return a new instance
     */
    public static JsonDecoder jsonDecoder() {
        return new JsonDecoder();
    }

    /**
     * Decodes the provided byte array into an object.
     *
     * @param bytes           the encoded bytes to be decoded
     * @param decodingContext the decoding context
     * @return the decoded object
     */
    @Override public Object apply(final byte[] bytes, final DecodingContext decodingContext) {
        return bytes != null && bytes.length > 0 ? new JsonSlurper().parse(bytes) : null;
    }
}
