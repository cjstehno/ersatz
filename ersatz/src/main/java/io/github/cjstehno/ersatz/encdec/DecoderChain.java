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
package io.github.cjstehno.ersatz.encdec;

import io.github.cjstehno.ersatz.cfg.ContentType;

import java.util.function.BiFunction;

/**
 * A function chain for request decoders.
 */
public class DecoderChain {

    private final RequestDecoders serverLevel;
    private final RequestDecoders requestLevel;

    /**
     * Creates a chain of decoders with the specified decoders.
     *
     * @param serverLevel  the server-defined request decoders
     * @param requestLevel the request-defined request decoders
     */
    public DecoderChain(final RequestDecoders serverLevel, final RequestDecoders requestLevel) {
        this.serverLevel = serverLevel != null ? serverLevel : new RequestDecoders();
        this.requestLevel = requestLevel != null ? requestLevel : new RequestDecoders();
    }

    /**
     * Resolves the decoder for the specified request content-type.
     *
     * @param contentType the request content-type
     * @return the decoder function
     */
    public BiFunction<byte[], DecodingContext, Object> resolve(final String contentType) {
        var found = requestLevel.findDecoder(contentType);
        if (found == null) {
            found = serverLevel.findDecoder(contentType);
        }
        return found;
    }

    /**
     * Resolves the decoder for the specified request content-type.
     *
     * @param contentType the request content-type
     * @return the decoder function
     */
    public BiFunction<byte[], DecodingContext, Object> resolve(final ContentType contentType) {
        return resolve(contentType.getValue());
    }

    /**
     * Resolves the decoder for the specified request content-type from the "server level" decoders.
     *
     * @param contentType the request content type
     * @return the decoder function
     */
    BiFunction<byte[], DecodingContext, Object> resolveServerLevel(final ContentType contentType) {
        return serverLevel.findDecoder(contentType);
    }
}
