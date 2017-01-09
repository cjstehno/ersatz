/*
 * Copyright (C) 2017 Christopher J. Stehno
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

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.ResponseEncoders

import java.util.function.Function

/**
 * A function chain for response encoders.
 */
class EncoderChain extends FunctionChain<ResponseEncoders> {

    EncoderChain(final ResponseEncoders firstItem = null) {
        super(firstItem)
    }

    /**
     * Resolves the encoder for the specified response content-type and object type.
     *
     * @param contentType the response content-type
     * @param objectType the response object type
     * @return the encoder
     */
    Function<Object, String> resolve(final String contentType, final Class objectType) {
        resolveWith { ResponseEncoders i -> i.findEncoder(contentType, objectType) } as Function<Object, String>
    }

    /**
     * Resolves the encoder for the specified response content-type and object type.
     *
     * @param contentType the response content-type
     * @param objectType the response object type
     * @return the encoder
     */
    Function<Object, String> resolve(final ContentType contentType, final Class objectType) {
        resolve contentType.value, objectType
    }
}