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

import com.stehno.ersatz.ClientRequest
import io.undertow.server.handlers.Cookie
import io.undertow.server.handlers.CookieImpl
import io.undertow.util.HeaderMap
import io.undertow.util.HttpString

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER

class MockClientRequest implements ClientRequest {

    String method
    String path
    byte[] body
    Map<String, Deque<String>> queryParams = [:]
    HeaderMap headers = [:]
    Map<String, Cookie> cookies = [:]
    long contentLength
    String characterEncoding

    MockClientRequest header(String name, String value) {
        headers.add(new HttpString(name), value)
        this
    }

    MockClientRequest query(String name, String... values) {
        queryParams.computeIfAbsent(name) { [] }.addAll(values as List)
        this
    }

    MockClientRequest cookie(String name, String value) {
        cookies[name] = new CookieImpl(name, value)
        this
    }

    void setContentType(final String contentType) {
        header(CONTENT_TYPE_HEADER, contentType)
    }

    @Override
    String getContentType() {
        headers.getFirst(CONTENT_TYPE_HEADER)
    }
}
