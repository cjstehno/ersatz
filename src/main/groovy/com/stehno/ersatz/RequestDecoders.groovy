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

import groovy.transform.Immutable

import javax.activation.MimeType
import java.util.function.BiFunction

/**
 * Configuration manager for a collection of request content decoders.
 */
class RequestDecoders {

    private final List<DecoderMapping> decoders = []

    /**
     * Creates a new decoder collection optionally registering decoders with the Groovy DSL closure.
     *
     * @param closure the optional configuration closure
     */
    RequestDecoders(@DelegatesTo(RequestDecoders) Closure closure = null) {
        if (closure) {
            closure.delegate = this
            closure.call()
        }
    }

    /**
     * Registers the decoder function with the collection
     *
     * @param contentType the content type
     * @param decoder the decoder function
     */
    void register(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        register contentType.value, decoder
    }

    /**
     * Registers the decoder function with the collection
     *
     * @param contentType the content type
     * @param decoder the decoder function
     */
    void register(final String contentType, final BiFunction<byte[], DecodingContext, Object> converter) {
        DecoderMapping existing = decoders.find { c -> c.mimeType.toString() == contentType }
        if (existing) {
            decoders.remove(existing)
        }

        decoders.add(new DecoderMapping(new MimeType(contentType), converter))
    }

    /**
     * Finds a decoder for the specified content type.
     *
     * @param contentType the content type
     * @return the decoder function
     */
    BiFunction<byte[], DecodingContext, Object> findDecoder(final ContentType contentType) {
        findDecoder(contentType.value)
    }

    /**
     * Finds a decoder for the specified content type.
     *
     * @param contentType the content type
     * @return the decoder function
     */
    BiFunction<byte[], DecodingContext, Object> findDecoder(final String contentType) {
        MimeType mimeType = new MimeType(contentType)

        def found = decoders.findAll { c -> c.mimeType.match(mimeType) }

        if (found.size() > 1) {
            found = [found.find { c -> c.mimeType.toString() == contentType }]
        }

        found[0]?.decoder
    }

    @Immutable(knownImmutableClasses = [MimeType, BiFunction])
    private static class DecoderMapping {

        MimeType mimeType
        BiFunction<byte[], DecodingContext, Object> decoder
    }
}