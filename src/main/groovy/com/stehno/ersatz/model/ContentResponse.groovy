/**
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
package com.stehno.ersatz.model

import com.stehno.ersatz.Response
import groovy.transform.CompileStatic

@CompileStatic
class ContentResponse implements Response {

    private final Map<String, String> headers = new HashMap<>()
    private final Map<String, String> cookies = new HashMap<>()
    private Object body
    private Integer code = 200

    ContentResponse body(final Object content) {
        this.body = content
        this
    }

    // TODO: support for more complex headers
    ContentResponse header(final String name, final String value) {
        headers[name] = value
        this
    }

    ContentResponse cookie(final String name, final String value) {
        cookies[name] = value
        this
    }

    ContentResponse contentType(final String contentType) {
        header('Content-Type', contentType)
        this
    }

    ContentResponse code(int code) {
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
