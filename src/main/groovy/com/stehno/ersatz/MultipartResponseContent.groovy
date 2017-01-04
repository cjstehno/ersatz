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
@CompileStatic
class MultipartResponseContent {

    private static final String ALPHANUMERICS = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    private final List<MultipartPart> parts = []
    private String boundary = generateBoundary()
    private final ResponseEncoders encoders = new ResponseEncoders()

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
    @SuppressWarnings('ConfusingMethodName')
    MultipartResponseContent encoders(final ResponseEncoders responseEncoders) {
        this.encoders.parent = responseEncoders
        this
    }

    /**
     * Used to override the default random boundary value with the provided one.
     *
     * @param value the boundary label to be used
     * @return a reference to this MultipartResponseContent instance
     */
    @SuppressWarnings('ConfusingMethodName')
    MultipartResponseContent boundary(final String value) {
        this.boundary = value
        this
    }

    /**
     * Configures a response content encoder for the specified contentType and content class.
     *
     * @param contentType the response content-type
     * @param type the response object type
     * @param closure the closure to be used as the encoder
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent encoder(final String contentType, final Class type, final Closure<String> closure) {
        encoder(contentType, type, closure as Function<Object, String>)
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
        encoders.register(contentType, type, encoder)
        this
    }

    /**
     * Configures a response content encoder for the specified contentType and content class.
     *
     * @param contentType the response content-type
     * @param type the response object type
     * @param closure the closure to be used as the encoder
     * @return a reference to this MultipartResponseContent instance
     */
    MultipartResponseContent encoder(final ContentType contentType, final Class type, final Closure<String> closure) {
        encoder(contentType.value, type, closure as Function<Object, String>)
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
        encoders.register(contentType.value, type, encoder)
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
        "multipart/mixed; boundary=$boundary"
    }

    /**
     * Renders the multipart body content as a String, ready for transfer. The configured encoders will be used to encode the part content.
     *
     * @return the multipart content as a String
     */
    @Override String toString() {
        StringBuilder out = new StringBuilder()

        parts.each { p ->
            out.append("--$boundary\r\n")

            if (p.fileName) {
                out.append("Content-Disposition: form-data; name=\"${p.fieldName}\"; filename=\"${p.fileName}\"\r\n")
            } else {
                out.append("Content-Disposition: form-data; name=\"${p.fieldName}\"\r\n")
            }

            if (p.transferEncoding) {
                out.append("Content-Transfer-Encoding: ${p.transferEncoding}\r\n")
            }

            out.append("Content-Type: ${p.contentType}\r\n\r\n")

            out.append(encode(p.contentType as String, p.value)).append('\r\n')
        }

        out.append("--${boundary}--\r\n")

        out.toString()
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

    private String encode(final String contentType, final Object obj) {
        def encoder = encoders.findEncoder(contentType, obj.class)
        if (encoder) {
            return encoder.apply(obj)
        }

        throw new IllegalArgumentException("No encoder found for content-type ($contentType) and object type (${obj.class.simpleName}).")
    }
}
