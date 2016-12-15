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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ContentType
import groovy.transform.Immutable

import javax.activation.MimeType
import java.util.function.Function

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

/**
 * Content converters for request body content based on content-type.
 *
 * Instances of this class should not be used directly, but rather populated through the `RequestWithContent` `converter` method.
 */
class RequestContentConverters {

    private final List<MimeTypeConverter> converters = []

    RequestContentConverters(@DelegatesTo(RequestContentConverters) Closure closure) {
        closure.delegate = this
        closure.call()
    }

    void register(final ContentType contentType, final Function<byte[], Object> converter) {
        register contentType.value, converter
    }

    void register(final String contentType, final Function<byte[], Object> converter) {
        MimeTypeConverter existing = converters.find { c -> c.mimeType.toString() == contentType }
        if (existing) {
            converters.remove(existing)
        }

        converters.add(new MimeTypeConverter(new MimeType(contentType), converter))
    }

    Function<byte[], Object> findConverter(final ContentType contentType) {
        findConverter(contentType.value)
    }

    Function<byte[], Object> findConverter(final String contentType) {
        if (!contentType) {
            return findConverter(TEXT_PLAIN)
        }

        MimeType mimeType = new MimeType(contentType)

        def found = converters.findAll { c -> c.mimeType.match(mimeType) }

        if (found.size() > 1) {
            found = [found.find { c -> c.mimeType.toString() == contentType }]
        }

        found[0]?.converter ?: findConverter(TEXT_PLAIN)
    }

    @Immutable(knownImmutableClasses = [MimeType, Function])
    private static class MimeTypeConverter {

        MimeType mimeType
        Function<byte[], Object> converter
    }
}