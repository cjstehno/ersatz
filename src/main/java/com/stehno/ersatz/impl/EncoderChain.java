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
import com.stehno.ersatz.encdec.ResponseEncoders;

import java.util.function.Function;

/**
 * A function chain for response encoders.
 */
public class EncoderChain extends FunctionChain<ResponseEncoders> {

    public EncoderChain() {
        this(null);
    }

    public EncoderChain(final ResponseEncoders firstItem) {
        super(firstItem);
    }

    /**
     * Resolves the encoder for the specified response content-type and object type.
     *
     * @param contentType the response content-type
     * @param objectType  the response object type
     * @return the encoder
     */
    public Function<Object, String> resolve(final String contentType, final Class objectType) {
        return resolveWith(e -> e.findEncoder(contentType, objectType));
    }

    /**
     * Resolves the encoder for the specified response content-type and object type.
     *
     * @param contentType the response content-type
     * @param objectType  the response object type
     * @return the encoder
     */
    public Function<Object, String> resolve(final ContentType contentType, final Class objectType) {
        return resolve( contentType.getValue(), objectType);
    }
}