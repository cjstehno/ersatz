/*
 * Copyright (C) 2019 Christopher J. Stehno
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
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Defines the request body content for a multipart request. An instance of this class may be created directly or by using the Groovy DSL closure or
 * Consumer configuration methods to build up the expected multipart request parts.
 * <p>
 * If partial or more flexible content matching is preferred, the <code>MultipartRequestMatcher</code> class provides a Hamcrest matcher for matching
 * multipart content, based on the model presented by this class.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class MultipartRequestContent {

    private final Map<String, MultipartPart> parts = new LinkedHashMap<>();

    /**
     * Creates and configures a multipart request object using the Groovy DSL closure (delegated to an instance of MultipartRequestContent).
     *
     * @param closure the configuration closure
     * @return a configured instance of MultipartRequestContent
     */
    public static MultipartRequestContent multipart(@DelegatesTo(value = MultipartRequestContent.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return multipart(ConsumerWithDelegate.create(closure));
    }

    /**
     * Creates and configures a multipart request object using the provided consumer, which will be given an instance of the MultipartRequestContent
     * to configure. This instance will then be returned from the method.
     *
     * @param config the configuration consumer
     * @return a configured instance of MultipartRequestContent
     */
    public static MultipartRequestContent multipart(final Consumer<MultipartRequestContent> config) {
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
    public MultipartRequestContent part(String fieldName, String fileName, String contentType, Object value) {
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
    public MultipartRequestContent part(String fieldName, String fileName, ContentType contentType, Object value) {
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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(parts, ((MultipartRequestContent) o).parts);
    }

    @Override public int hashCode() {
        return Objects.hash(parts);
    }
}
