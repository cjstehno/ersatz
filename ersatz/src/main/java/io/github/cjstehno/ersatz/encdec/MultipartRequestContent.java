/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.encdec;

import io.github.cjstehno.ersatz.cfg.ContentType;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;

/**
 * Defines the request body content for a multipart request. An instance of this class may be created directly or by using the Groovy DSL closure or
 * Consumer configuration methods to build up the expected multipart request parts.
 * <p>
 * If partial or more flexible content matching is preferred, the <code>MultipartRequestMatcher</code> class provides a Hamcrest matcher for matching
 * multipart content, based on the model presented by this class.
 */
@EqualsAndHashCode
public class MultipartRequestContent {

    private final Map<String, MultipartPart> parts = new LinkedHashMap<>();

    /**
     * Creates and configures a multipart request object using the provided consumer, which will be given an instance of the MultipartRequestContent
     * to configure. This instance will then be returned from the method.
     *
     * @param config the configuration consumer
     * @return a configured instance of MultipartRequestContent
     */
    public static MultipartRequestContent multipartRequest(final Consumer<MultipartRequestContent> config) {
        MultipartRequestContent request = new MultipartRequestContent();
        config.accept(request);
        return request;
    }

    /**
     * Configures a field part with the given field name and value (as text/plain).
     *
     * @param fieldName the field name
     * @param value     the value
     * @return a reference to this multipart request instance
     */
    public MultipartRequestContent part(final String fieldName, final String value) {
        return part(fieldName, TEXT_PLAIN, value);
    }

    /**
     * Configures a part with the given field name and value as the specified content type.
     *
     * @param fieldName   the field name
     * @param contentType the content type
     * @param value       the value
     * @return a reference to this multipart request instance
     */
    public MultipartRequestContent part(final String fieldName, final String contentType, final Object value) {
        parts.put(fieldName, new MultipartPart(fieldName, null, contentType, null, value));
        return this;
    }

    /**
     * Configures a part with the given field name and value as the specified content type.
     *
     * @param fieldName   the field name
     * @param contentType the content type
     * @param value       the value
     * @return a reference to this multipart request instance
     */
    public MultipartRequestContent part(final String fieldName, final ContentType contentType, final Object value) {
        parts.put(fieldName, new MultipartPart(fieldName, null, contentType.getValue(), null, value));
        return this;
    }

    /**
     * Configures a part with the given field name, file name and value as the specified content type.
     *
     * @param fieldName   the field name
     * @param fileName    the file name
     * @param contentType the content type
     * @param value       the value
     * @return a reference to this multipart request instance
     */
    public MultipartRequestContent part(final String fieldName, final String fileName, final String contentType, final Object value) {
        parts.put(fieldName, new MultipartPart(fieldName, fileName, contentType, null, value));
        return this;
    }

    /**
     * Configures a part with the given field name, file name and value as the specified content type.
     *
     * @param fieldName   the field name
     * @param fileName    the file name
     * @param contentType the content type
     * @param value       the value
     * @return a reference to this multipart request instance
     */
    public MultipartRequestContent part(final String fieldName, final String fileName, final ContentType contentType, final Object value) {
        parts.put(fieldName, new MultipartPart(fieldName, fileName, contentType.getValue(), null, value));
        return this;
    }

    /**
     * Retrieves the part with the specified field name.
     *
     * @param fieldName the field name
     * @return the part or null if none is configured
     */
    public MultipartPart getAt(final String fieldName) {
        return parts.get(fieldName);
    }
}
