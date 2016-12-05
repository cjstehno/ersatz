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

import com.stehno.ersatz.Response
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic @TupleConstructor
class ErsatzResponse implements Response {

    final boolean empty

    private final Map<String, String> headers = [:]
    private final Map<String, String> cookies = [:]
    private Object body
    private Integer code = 200

    @SuppressWarnings('ConfusingMethodName')
    Response body(final Object content) {
        if (empty) {
            throw new IllegalArgumentException('The response is configured as EMPTY and cannot have content.')
        }

        this.body = content
        this
    }

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

    Response cookie(final String name, final String value) {
        cookies[name] = value
        this
    }

    Response contentType(final String contentType) {
        header('Content-Type', contentType)
        this
    }

    @Override
    String getContentType() {
        headers['Content-Type']
    }

    @SuppressWarnings('ConfusingMethodName')
    Response code(int code) {
        this.code = code
        this
    }

    Map<String, String> getHeaders() {
        headers.asImmutable()
    }

    Map<String, String> getCookies() {
        cookies.asImmutable()
    }

    Object getBody() {
        body
    }

    Integer getCode() {
        code
    }
}
