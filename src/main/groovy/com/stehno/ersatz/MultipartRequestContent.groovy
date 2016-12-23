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
package com.stehno.ersatz

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

import java.util.function.Consumer

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

/**
 * FIXME: document
 */
@CompileStatic @EqualsAndHashCode(includeFields = true)
class MultipartRequestContent {

    // FIXME: is there shared code between this and multipart response object?
    // FIXME: may want to reuse the part object here too
    // FIXME: consider adding typed methods rather than Object

    private final List<Map<String, Object>> parts = []

    static MultipartRequestContent multipart(@DelegatesTo(MultipartRequestContent) final Closure closure) {
        MultipartRequestContent request = new MultipartRequestContent()
        closure.delegate = request
        closure.call()
        request
    }

    static MultipartRequestContent multipart(final Consumer<MultipartRequestContent> config) {
        MultipartRequestContent request = new MultipartRequestContent()
        config.accept(request)
        request
    }

    MultipartRequestContent part(final String fieldName, final String value) {
        part fieldName, TEXT_PLAIN, value
    }

    MultipartRequestContent part(final String fieldName, final String contentType, final Object value) {
        parts << [fieldName: fieldName, contentType: contentType, value: value]
        this
    }

    MultipartRequestContent part(final String fieldName, final ContentType contentType, final Object value) {
        parts << [fieldName: fieldName, contentType: contentType.value, value: value]
        this
    }

    MultipartRequestContent part(String fieldName, String fileName, String contentType, Object value) {
        parts << [fieldName: fieldName, fileName: fileName, contentType: contentType, value: value]
        this
    }

    MultipartRequestContent part(String fieldName, String fileName, ContentType contentType, Object value) {
        parts << [fieldName: fieldName, fileName: fileName, contentType: contentType.value, value: value]
        this
    }

    protected Map<String, Object> getAt(String fieldName) {
        (parts.find { p -> p.fieldName == fieldName }?.asImmutable() ?: [:]) as Map<String, Object>
    }
}

