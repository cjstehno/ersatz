/**
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.encdec;

import com.stehno.ersatz.cfg.ContentType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;

/**
 * Implementation of the multipart response content interface.
 */
public class ErsatzMultipartResponseContent extends MultipartResponseContent {

    private final List<MultipartPart> parts = new LinkedList<>();
    private String boundaryTag = generateBoundary();
    private final ResponseEncoders localEncoders = new ResponseEncoders();
    private final EncoderChain encoderChain = new EncoderChain(localEncoders);

    public MultipartResponseContent encoders(final ResponseEncoders responseEncoders) {
        encoderChain.second(responseEncoders);
        return this;
    }

    public MultipartResponseContent boundary(final String value) {
        this.boundaryTag = value;
        return this;
    }

    public MultipartResponseContent encoder(final String contentType, final Class type, final Function<Object, byte[]> encoder) {
        localEncoders.register(contentType, type, encoder);
        return this;
    }

    public MultipartResponseContent encoder(final ContentType contentType, final Class type, final Function<Object, byte[]> encoder) {
        localEncoders.register(contentType.getValue(), type, encoder);
        return this;
    }

    public MultipartResponseContent field(final String fieldName, final String value) {
        return part(fieldName, TEXT_PLAIN, value);
    }

    public MultipartResponseContent part(final String fieldName, final String contentType, final Object value) {
        parts.add(new MultipartPart(fieldName, null, contentType, null, value));
        return this;
    }

    public MultipartResponseContent part(final String fieldName, final ContentType contentType, final Object value, final String transferEncoding) {
        parts.add(new MultipartPart(fieldName, null, contentType.getValue(), transferEncoding, value));
        return this;
    }

    public MultipartResponseContent part(final String fieldName, final ContentType contentType, final Object value) {
        return part(fieldName, contentType, value, null);
    }

    public MultipartResponseContent part(String fieldName, String fileName, String contentType, Object value, String transferEncoding) {
        parts.add(new MultipartPart(fieldName, fileName, contentType, transferEncoding, value));
        return this;
    }

    public MultipartResponseContent part(String fieldName, String fileName, String contentType, Object value) {
        return part(fieldName, fileName, contentType, value, null);
    }

    public MultipartResponseContent part(String fieldName, String fileName, ContentType contentType, Object value, String transferEncoding) {
        parts.add(new MultipartPart(fieldName, fileName, contentType.getValue(), transferEncoding, value));
        return this;
    }

    public MultipartResponseContent part(String fieldName, String fileName, ContentType contentType, Object value) {
        return part(fieldName, fileName, contentType, value, null);
    }

    /**
     * Used to retrieve the response content-type, including the configured boundary label.
     *
     * @return the response content-type and boundary label
     */
    public String getContentType() {
        return "multipart/mixed; boundary=" + boundaryTag;
    }

    /**
     * Retrieves the multipart boundary tag.
     *
     * @return the boundary tag
     */
    public String getBoundary() {
        return boundaryTag;
    }

    /**
     * Provides an immutable iterator over the parts.
     *
     * @return an immutable part iterator
     */
    public Iterable<MultipartPart> parts() {
        return Collections.unmodifiableList(parts);
    }

    /**
     * Resolves the encoder function for the specified content-type and object-type being encoded.
     *
     * @param contentType the content-type being encoded
     * @param objectType the type of object being encoded
     * @return the encoder function for the criteria
     * @throws IllegalArgumentException if no encoder is found for the content-type and object-type
     */
    public Function<Object, byte[]> encoder(final String contentType, final Class objectType) {
        final var encoder = encoderChain.resolve(contentType, objectType);
        if (encoder != null) {
            return encoder;
        }

        throw new IllegalArgumentException("No encoder found for content-type (" + contentType + ") and object type (" + objectType.getSimpleName() + ").");
    }
}
