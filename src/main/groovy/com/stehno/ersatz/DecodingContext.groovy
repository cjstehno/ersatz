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
package com.stehno.ersatz

import com.stehno.ersatz.impl.DecoderChain
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

/**
 * Request-specific contextual information used by request content decoders.
 */
@CompileStatic @TupleConstructor
class DecodingContext {

    /**
     * The request content length.
     */
    final long contentLength

    /**
     * The request content type.
     */
    final String contentType

    /**
     * The request character encoding.
     */
    final String characterEncoding

    /**
     * The available request decoders.
     */
    final DecoderChain decoderChain
}
