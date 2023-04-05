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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * A function chain for response encoders.
 */
public class EncoderChain {

    private final ResponseEncoders serverLevel;
    private final ResponseEncoders responseLevel;

    /**
     * Creates a new encoder chain with the provided encoders.
     *
     * @param serverLevel   the server configured response encoders
     * @param responseLevel the response configured response encoders
     */
    public EncoderChain(final ResponseEncoders serverLevel, final ResponseEncoders responseLevel) {
        this.serverLevel = serverLevel != null ? serverLevel : new ResponseEncoders();
        this.responseLevel = responseLevel != null ? responseLevel : new ResponseEncoders();
    }

    /**
     * Resolves the encoder for the specified response content-type and object type.
     *
     * @param contentType the response content-type
     * @param objectType  the response object type
     * @return the encoder
     */
    public Function<Object, byte[]> resolve(final String contentType, final Class objectType) {
        var found = responseLevel.findEncoder(contentType, objectType);
        if (found == null) {
            found = serverLevel.findEncoder(contentType, objectType);
        }
        return found;
    }

    /**
     * Resolves the encoder for the specified response content-type and object type.
     *
     * @param contentType the response content-type
     * @param objectType  the response object type
     * @return the encoder
     */
    public Function<Object, byte[]> resolve(final ContentType contentType, final Class objectType) {
        return resolve(contentType.getValue(), objectType);
    }

    /**
     * Resolves the encoder for the specified response content-type and object type from the server level encoders.
     *
     * @param contentType the response content-type
     * @param objectType  the response object type
     * @return the encoder
     */
    public Function<Object, byte[]> resolveServerLevel(final String contentType, final Class objectType) {
        return serverLevel.findEncoder(contentType, objectType);
    }

    /**
     * Resolves the encoder for the specified response content-type and object type from the server level encoders.
     *
     * @param contentType the response content-type
     * @param objectType  the response object type
     * @return the encoder
     */
    public Function<Object, byte[]> resolveServerLevel(final ContentType contentType, final Class objectType) {
        return serverLevel.findEncoder(contentType, objectType);
    }

    /**
     * Retrieves an ordered collection containing the server-level and response-level encoders.
     *
     * @return a list of the encoder configurations
     */
    public Collection<ResponseEncoders> items() {
        return List.of(serverLevel, responseLevel);
    }
}