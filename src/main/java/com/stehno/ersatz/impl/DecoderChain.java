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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.ContentType;
import com.stehno.ersatz.encdec.DecodingContext;
import com.stehno.ersatz.encdec.RequestDecoders;

import java.util.function.BiFunction;

/**
 * A function chain for request decoders.
 */
public class DecoderChain extends FunctionChain<RequestDecoders> {

    public DecoderChain() {
        this(null);
    }

    public DecoderChain(final RequestDecoders firstItem) {
        super(firstItem);
    }

    /**
     * Resolves the decoder for the specified request content-type.
     *
     * @param contentType the request content-type
     * @return the decoder function
     */
    public BiFunction<byte[], DecodingContext, Object> resolve(final String contentType) {
        return resolveWith(d -> d.findDecoder(contentType));
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
}

