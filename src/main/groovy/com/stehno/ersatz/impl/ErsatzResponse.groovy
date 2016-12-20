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

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.MultipartContent
import com.stehno.ersatz.Response
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

/**
 * Implementation of the <code>Response</code> interface.
 */
@CompileStatic @TupleConstructor
class ErsatzResponse implements Response {

    static final String CONTENT_TYPE_HEADER = 'Content-Type'

    /**
     * Whether or not this instance defines an empty response.
     */
    final boolean empty

    private final Map<String, String> headers = [:]
    private final Map<String, String> cookies = [:]
    private Object content
    private Integer code = 200

    @Override @SuppressWarnings('ConfusingMethodName')
    Response content(final Object content) {
        if (empty) {
            throw new IllegalArgumentException('The response is configured as EMPTY and cannot have content.')
        }

        this.content = content

        if (content instanceof MultipartContent) {
            contentType(content.contentType)
        }

        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Response content(final Object content, final String contentType) {
        this.content(content)
        this.contentType(contentType)
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Response content(final Object content, final ContentType contentType) {
        this.content(content, contentType.value)
    }

    @Override
    Response header(final String name, final String value) {
        headers[name] = value
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Response headers(final Map<String, String> headers) {
        this.headers.putAll(headers)
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Response cookies(final Map<String, String> cookies) {
        this.cookies.putAll(cookies)
        this
    }

    @Override
    Response cookie(final String name, final String value) {
        cookies[name] = value
        this
    }

    @Override
    Response contentType(final String contentType) {
        header(CONTENT_TYPE_HEADER, contentType)
        this
    }

    @Override
    Response contentType(final ContentType contentType) {
        header(CONTENT_TYPE_HEADER, contentType.value)
        this
    }

    @Override
    String getContentType() {
        headers[CONTENT_TYPE_HEADER]
    }

    @SuppressWarnings('ConfusingMethodName')
    Response code(int code) {
        this.code = code
        this
    }

    @Override
    Map<String, String> getHeaders() {
        headers.asImmutable()
    }

    @Override
    Map<String, String> getCookies() {
        cookies.asImmutable()
    }

    @Override
    Object getContent() {
        content
    }

    @Override
    Integer getCode() {
        code
    }
}
