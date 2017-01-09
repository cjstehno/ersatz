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
import com.stehno.ersatz.MultipartResponseContent
import com.stehno.ersatz.ResponseEncoders
import groovy.transform.CompileStatic

import java.util.function.Function

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

/**
 * Implementation of the multipart response content interface.
 */
@CompileStatic @SuppressWarnings('ConfusingMethodName')
class ErsatzMultipartResponseContent extends MultipartResponseContent {

    private final List<MultipartPart> parts = []
    private String boundaryTag = generateBoundary()
    private final ResponseEncoders localEncoders = new ResponseEncoders()
    private final EncoderChain encoderChain = new EncoderChain(localEncoders)

    MultipartResponseContent encoders(final ResponseEncoders responseEncoders) {
        encoderChain.second(responseEncoders)
        this
    }

    MultipartResponseContent boundary(final String value) {
        this.boundaryTag = value
        this
    }

    MultipartResponseContent encoder(final String contentType, final Class type, final Function<Object, String> encoder) {
        localEncoders.register(contentType, type, encoder)
        this
    }

    MultipartResponseContent encoder(final ContentType contentType, final Class type, final Function<Object, String> encoder) {
        localEncoders.register(contentType.value, type, encoder)
        this
    }

    MultipartResponseContent field(final String fieldName, final String value) {
        part fieldName, TEXT_PLAIN, value
    }

    MultipartResponseContent part(final String fieldName, final String contentType, final Object value) {
        parts << new MultipartPart(fieldName, null, contentType, null, value)
        this
    }

    MultipartResponseContent part(final String fieldName, final ContentType contentType, final Object value, final String transferEncoding = null) {
        parts << new MultipartPart(fieldName, null, contentType.value, transferEncoding, value)
        this
    }

    MultipartResponseContent part(String fieldName, String fileName, String contentType, Object value, String transferEncoding = null) {
        parts << new MultipartPart(fieldName, fileName, contentType, transferEncoding, value)
        this
    }

    MultipartResponseContent part(String fieldName, String fileName, ContentType contentType, Object value, String transferEncoding = null) {
        parts << new MultipartPart(fieldName, fileName, contentType.value, transferEncoding, value)
        this
    }

    /**
     * Used to retrieve the response content-type, including the configured boundary label.
     *
     * @return the response content-type and boundary label
     */
    String getContentType() {
        "multipart/mixed; boundary=$boundaryTag"
    }

    /**
     * Retrieves the multipart boundary tag.
     *
     * @return the boundary tag
     */
    String getBoundary() { boundaryTag }

    /**
     * Provides an immutable iterator over the parts.
     *
     * @return an immutable part iterator
     */
    Iterable<MultipartPart> parts() {
        parts.asImmutable()
    }

    Function<Object, String> encoder(final String contentType, final Class objectType) {
        Function<Object, String> encoder = encoderChain.resolve(contentType, objectType)

        if (encoder) {
            return encoder
        }

        throw new IllegalArgumentException("No encoder found for content-type ($contentType) and object type (${objectType.simpleName}).")
    }
}
