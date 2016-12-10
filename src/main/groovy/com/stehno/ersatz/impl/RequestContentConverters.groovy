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

import groovy.transform.Immutable

import javax.activation.MimeType
import java.util.function.Function

/**
 * Content converters for request body content based on content-type.
 */
class RequestContentConverters {

    // FIXME: needs testing

    private static final String DEFAULT_CONTENT_TYPE = 'text/plain'
    private final List<MimeTypeConverter> converters = []

    RequestContentConverters(@DelegatesTo(RequestContentConverters) Closure closure) {
        closure.delegate = this
        closure.call()
    }

    void register(final MimeType contentType, final Function<byte[], Object> converter) {
        converters.add(new MimeTypeConverter(contentType, converter))
    }

    Function<byte[], Object> findConverter(final String contentType) {
        if (!contentType) {
            return findConverter(DEFAULT_CONTENT_TYPE)
        }

        MimeType mimeType = new MimeType(contentType)
        converters.find { c -> c.mimeType.match(mimeType) }?.converter ?: findConverter(DEFAULT_CONTENT_TYPE)
    }
}

@Immutable(knownImmutableClasses = [MimeType, Function])
class MimeTypeConverter {

    MimeType mimeType
    Function<byte[], Object> converter
}