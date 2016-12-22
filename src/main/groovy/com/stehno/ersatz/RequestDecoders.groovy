/*
 * Copyright (C) 2016 Christopher J. Stehno
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
 * Content converters for request body content based on content-type.
 *
 * Instances of this class should not be used directly, but rather populated through the `RequestWithContent` `converter` method.
 *
 * FIXME: update and document for public api
 */
class RequestDecoders {

    private final List<DecoderMapping> converters = []
    private RequestDecoders parent

    RequestDecoders(@DelegatesTo(RequestDecoders) Closure closure = null) {
        if (closure) {
            closure.delegate = this
            closure.call()
        }
    }

    void setParent(final RequestDecoders parent) {
        this.parent = parent
    }

    void register(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> converter) {
        register contentType.value, converter
    }

    void register(final String contentType, final BiFunction<byte[], DecodingContext, Object> converter) {
        DecoderMapping existing = converters.find { c -> c.mimeType.toString() == contentType }
        if (existing) {
            converters.remove(existing)
        }

        converters.add(new DecoderMapping(new MimeType(contentType), converter))
    }

    BiFunction<byte[], DecodingContext, Object> findDecoder(final ContentType contentType) {
        findDecoder(contentType.value)
    }

    BiFunction<byte[], DecodingContext, Object> findDecoder(final String contentType) {
        MimeType mimeType = new MimeType(contentType)

        def found = converters.findAll { c -> c.mimeType.match(mimeType) }

        if (found.size() > 1) {
            found = [found.find { c -> c.mimeType.toString() == contentType }]
        }

        def decoder = found[0]?.decoder ?: (parent ? parent.findDecoder(contentType) : null)

        if (decoder) {
            return decoder
        } else {
            throw new IllegalArgumentException("No decoder was found for content-type (${contentType}) - did you configure one?")
        }
    }

    @Immutable(knownImmutableClasses = [MimeType, BiFunction])
    private static class DecoderMapping {

        MimeType mimeType
        BiFunction<byte[], DecodingContext, Object> decoder
    }
}