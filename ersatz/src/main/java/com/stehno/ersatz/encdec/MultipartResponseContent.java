/**
 * Copyright (C) 2020 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.encdec;

import com.stehno.ersatz.cfg.ContentType;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Response content object used to return a multipart response to a request. Note that multipart responses are not reliably supported by most
 * browsers; this feature is mainly intended to support RESTful interfaces that may want to implement multipart response content.
 * <p>
 * When configuring multipart content, encoders must be provided to convert the content objects into the serialized transfer format. If a shared
 * <code>ResponseEncoders</code> is provided, they will be used as defaults and overridden by any encoders specified on the response configuration
 * itself.
 * <p>
 * Note that the globally configured encoders will be injected when this content object is added to the response body.
 */
public abstract class MultipartResponseContent {

    private static final char[] ALPHANUMERICS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /**
     * Creates a new multipart response content object with the optional boundary (random default) and a Consumer used to configure the parts. The
     * Consumer will have an instance of MultipartContent passed into it for configuration.
     *
     * @param consumer the configuration consumer (given an instance of MultipartContent)
     * @return a reference to this MultipartResponseContent instance
     */
    public static MultipartResponseContent multipartResponse(final Consumer<MultipartResponseContent> consumer) {
        MultipartResponseContent content = new ErsatzMultipartResponseContent();
        consumer.accept(content);
        return content;
    }

    /**
     * Used to generate a random boundary tag.
     *
     * @return a random boundary label
     */
    public static String generateBoundary() {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 18; i++) {
            buffer.append(ALPHANUMERICS[ThreadLocalRandom.current().nextInt(0, ALPHANUMERICS.length)]);
        }
        return buffer.toString();
    }

    /**
     * Used to specify the set of shared (parent) encoders used - this encoder collection will be called when no encoder is specified in the current
     * response configuration.
     *
     * @param responseEncoders the parent set of shared encoders
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent encoders(final ResponseEncoders responseEncoders);

    /**
     * Used to override the default random boundary value with the provided one.
     *
     * @param value the boundary label to be used
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent boundary(final String value);

    /**
     * Configures a response content encoder for the specified contentType and content class.
     *
     * @param contentType the response content-type
     * @param type        the response object type
     * @param encoder     the encoder
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent encoder(final String contentType, final Class type, final Function<Object, byte[]> encoder);

    /**
     * Configures a response content encoder for the specified contentType and content class.
     *
     * @param contentType the response content-type
     * @param type        the response object type
     * @param encoder     the encoder
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent encoder(final ContentType contentType, final Class type, final Function<Object, byte[]> encoder);

    /**
     * Used to add a "field" part to the response.
     *
     * @param fieldName the field name
     * @param value     the field value
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent field(final String fieldName, final String value);

    /**
     * Used to add a "field" part to the response.
     *
     * @param fieldName   the field name
     * @param contentType the response part content-type
     * @param value       the field value
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent part(final String fieldName, final String contentType, final Object value);

    /**
     * Used to add a "field" part to the response.
     *
     * @param fieldName        the field name
     * @param contentType      the response part content-type
     * @param value            the field value
     * @param transferEncoding the content-transfer-encoding value (defaults to none)
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent part(final String fieldName, final ContentType contentType, final Object value, final String transferEncoding);

    public abstract MultipartResponseContent part(final String fieldName, final ContentType contentType, final Object value);

    /**
     * Used to add a "file" part to the response.
     *
     * @param fieldName        the field name
     * @param fileName         the file name
     * @param contentType      the response part content-type
     * @param value            the field value
     * @param transferEncoding the content-transfer-encoding value (defaults to none)
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent part(String fieldName, String fileName, String contentType, Object value, String transferEncoding);

    public abstract MultipartResponseContent part(String fieldName, String fileName, String contentType, Object value);

    /**
     * Used to add a "file" part to the response.
     *
     * @param fieldName        the field name
     * @param fileName         the file name
     * @param contentType      the response part content-type
     * @param value            the field value
     * @param transferEncoding the content-transfer-encoding value (defaults to none)
     * @return a reference to this MultipartResponseContent instance
     */
    public abstract MultipartResponseContent part(String fieldName, String fileName, ContentType contentType, Object value, String transferEncoding);

    public abstract MultipartResponseContent part(String fieldName, String fileName, ContentType contentType, Object value);
}

