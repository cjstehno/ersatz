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

import com.stehno.ersatz.impl.EncoderChain
import com.stehno.ersatz.impl.MultipartPart
import groovy.transform.CompileStatic

import java.util.function.Consumer
import java.util.function.Function

import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static java.util.Collections.shuffle

/**
 * Response content object used to return a multipart response to a request. Note that multipart responses are not reliably supported by most
 * browsers; this feature is mainly intended to support RESTful interfaces that may want to implement multipart response content.
 *
 * When configuring multipart content, encoders must be provided to convert the content objects into the serialized transfer format. If a shared
 * <code>ResponseEncoders</code> is provided, they will be used as defaults and overridden by any encoders specified on the response configuration
 * itself.
 */
@CompileStatic @SuppressWarnings('ConfusingMethodName')
class MultipartResponseContent {

    private static final String ALPHANUMERICS = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    private final List<MultipartPart> parts = []
    private String boundaryTag = generateBoundary()
    private final ResponseEncoders localEncoders = new ResponseEncoders()
    private final EncoderChain encoderChain = new EncoderChain(localEncoders)

    /**
     * Creates a new multipart response content object with the optional boundary (random default) and a Closure used to configure the parts.
     *
     * @param closure the configuration closure (Delegates to MultipartContent instance)
     * @return a reference to this MultipartResponseContent instance
     */
    static MultipartResponseContent multipart(final @DelegatesTo(MultipartResponseContent) Closure closure) {
        MultipartResponseContent content = new MultipartResponseContent()
        closure.delegate = content
        closure.call()
        content
    }

    /**
     * Creates a new multipart response content object with the optional boundary (random default) and a Consumer used to configure the parts. The
     * Consumer will have an instance of MultipartContent passed into it for configuration.
     *
     * @param closure the configuration consumer (given an instance of MultipartContent)
     * @return a reference to this MultipartResponseContent instance
     */
    static MultipartResponseContent multipart(final Consumer<MultipartResponseContent> consumer) {
        MultipartResponseContent content = new MultipartResponseContent()
        consumer.accept(content)
        content
    }

    /**
     * Used to specify the set of shared (parent) encoders used - this encoder collection will be called when no encoder is specified in the current
     * response configuration.
     *
     * @param responseEncoders the parent set of shared encoders
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent encoders(final ResponseEncoders responseEncoders) {
        encoderChain.second(responseEncoders)
        this
    }

    /**
     * Used to override the default random boundary value with the provided one.
     *
     * @param value the boundary label to be used
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent boundary(final String value) {
        this.boundaryTag = value
        this
    }

    /**
     * Configures a response content encoder for the specified contentType and content class.
     *
     * @param contentType the response content-type
     * @param type the response object type
     * @param encoder the encoder
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent encoder(final String contentType, final Class type, final Function<Object, String> encoder) {
        localEncoders.register(contentType, type, encoder)
        this
    }

    /**
     * Configures a response content encoder for the specified contentType and content class.
     *
     * @param contentType the response content-type
     * @param type the response object type
     * @param encoder the encoder
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent encoder(final ContentType contentType, final Class type, final Function<Object, String> encoder) {
        localEncoders.register(contentType.value, type, encoder)
        this
    }

    /**
     * Used to add a "field" part to the response.
     *
     * @param fieldName the field name
     * @param value the field value
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent field(final String fieldName, final String value) {
        part fieldName, TEXT_PLAIN, value
    }

    /**
     * Used to add a "field" part to the response.
     *
     * @param fieldName the field name
     * @param contentType the response part content-type
     * @param value the field value
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent part(final String fieldName, final String contentType, final Object value) {
        parts << new MultipartPart(fieldName, null, contentType, null, value)
        this
    }

    /**
     * Used to add a "field" part to the response.
     *
     * @param fieldName the field name
     * @param contentType the response part content-type
     * @param value the field value
     * @param transferEncoding the content-transfer-encoding value (defaults to none)
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent part(final String fieldName, final ContentType contentType, final Object value, final String transferEncoding = null) {
        parts << new MultipartPart(fieldName, null, contentType.value, transferEncoding, value)
        this
    }

    /**
     * Used to add a "file" part to the response.
     *
     * @param fieldName the field name
     * @param fileName the file name
     * @param contentType the response part content-type
     * @param value the field value
     * @param transferEncoding the content-transfer-encoding value (defaults to none)
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent part(String fieldName, String fileName, String contentType, Object value, String transferEncoding = null) {
        parts << new MultipartPart(fieldName, fileName, contentType, transferEncoding, value)
        this
    }

    /**
     * Used to add a "file" part to the response.
     *
     * @param fieldName the field name
     * @param fileName the file name
     * @param contentType the response part content-type
     * @param value the field value
     * @param transferEncoding the content-transfer-encoding value (defaults to none)
     * @return a reference to this MultipartResponseContent instance
     */
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

    // TODO: not really happy about having this here (on public api)
    Function<Object, String> encoder(final String contentType, final Class objectType) {
        Function<Object, String> encoder = encoderChain.resolve(contentType, objectType)

        if (encoder) {
            return encoder
        }

        throw new IllegalArgumentException("No encoder found for content-type ($contentType) and object type (${objectType.simpleName}).")
    }

    /**
     * Used to generate a random boundary label.
     *
     * @return a random boundary label
     */
    static String generateBoundary() {
        def letters = ALPHANUMERICS as List
        shuffle(letters)
        letters[0..<18].join('')
    }
}
