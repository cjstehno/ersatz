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
import groovy.transform.EqualsAndHashCode

import java.util.function.Consumer

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

/**
 * Defines the request body content for a multipart request. An instance of this class may be created directly or by using the Groovy DSL closure or
 * Consumer configuration methods to build up the expected multipart request parts.
 *
 * If partial or more flexible content matching is preferred, the <code>MultipartRequestMatcher</code> class provides a Hamcrest matcher for matching
 * multipart content, based on the model presented by this class.
 */
@CompileStatic @EqualsAndHashCode(includeFields = true)
class MultipartRequestContent {

    private final Map<String, MultipartPart> parts = [:]

    /**
     * Creates and configures a multipart request object using the Groovy DSL closure (delegated to an instance of MultipartRequestContent).
     *
     * @param closure the configuration closure
     * @return a configured instance of MultipartRequestContent
     */
    static MultipartRequestContent multipart(@DelegatesTo(MultipartRequestContent) final Closure closure) {
        MultipartRequestContent request = new MultipartRequestContent()
        closure.delegate = request
        closure.call()
        request
    }

    /**
     * Creates and configures a multipart request object using the provided consumer, which will be given an instance of the MultipartRequestContent
     * to configure. This instance will then be returned from the method.
     *
     * @param closure the configuration consumer
     * @return a configured instance of MultipartRequestContent
     */
    static MultipartRequestContent multipart(final Consumer<MultipartRequestContent> config) {
        MultipartRequestContent request = new MultipartRequestContent()
        config.accept(request)
        request
    }

    /**
     * Configures a field part with the given field name and value (as text/plain).
     *
     * @param fieldName the field name
     * @param value the value
     * @return a reference to this multipart request instance
     */
    MultipartRequestContent part(final String fieldName, final String value) {
        part fieldName, TEXT_PLAIN, value
    }

    /**
     * Configures a part with the given field name and value as the specified content type.
     *
     * @param fieldName the field name
     * @param contentType the content type
     * @param value the value
     * @return a reference to this multipart request instance
     */
    MultipartRequestContent part(final String fieldName, final String contentType, final Object value) {
        parts[fieldName] = new MultipartPart(fieldName, null, contentType, null, value)
        this
    }

    /**
     * Configures a part with the given field name and value as the specified content type.
     *
     * @param fieldName the field name
     * @param contentType the content type
     * @param value the value
     * @return a reference to this multipart request instance
     */
    MultipartRequestContent part(final String fieldName, final ContentType contentType, final Object value) {
        parts[fieldName] = new MultipartPart(fieldName, null, contentType.value, null, value)
        this
    }

    /**
     * Configures a part with the given field name, file name and value as the specified content type.
     *
     * @param fieldName the field name
     * @param fileName the file name
     * @param contentType the content type
     * @param value the value
     * @return a reference to this multipart request instance
     */
    MultipartRequestContent part(String fieldName, String fileName, String contentType, Object value) {
        parts[fieldName] = new MultipartPart(fieldName, fileName, contentType, null, value)
        this
    }

    /**
     * Configures a part with the given field name, file name and value as the specified content type.
     *
     * @param fieldName the field name
     * @param fileName the file name
     * @param contentType the content type
     * @param value the value
     * @return a reference to this multipart request instance
     */
    MultipartRequestContent part(String fieldName, String fileName, ContentType contentType, Object value) {
        parts[fieldName] = new MultipartPart(fieldName, fileName, contentType.value, null, value)
        this
    }

    /**
     * Retrieves the part with the specified field name.
     *
     * @param fieldName the field name
     * @return the part or null if none is configured
     */
    protected MultipartPart getAt(final String fieldName) {
        parts[fieldName]
    }
}
