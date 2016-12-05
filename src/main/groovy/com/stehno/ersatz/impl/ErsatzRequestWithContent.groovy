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

import com.stehno.ersatz.RequestWithContent
import groovy.json.JsonParser
import groovy.transform.CompileStatic
import io.undertow.server.HttpServerExchange

import java.util.function.Function

import static com.stehno.ersatz.Conditions.bodyEquals

/**
 * Ersatz implementation of a <code>Request</code> with body content.
 */
@CompileStatic
class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    public static final String CONTENT_TYPE_HEADER = 'Content-Type'

    private static final String DEFAULT_CONTENT_TYPE = 'text/plain; charset=utf-8'

    // TODO: a better way to map these would be nice - reduce duplication
    private final Map<String, Function<byte[], Object>> converters = [
        'text/plain'                : { byte[] m -> new String(m, 'UTF-8') } as Function<byte[], Object>,
        'text/plain; charset=utf-8' : { byte[] m -> new String(m, 'UTF-8') } as Function<byte[], Object>,
        'text/plain; charset=utf-16': { byte[] m -> new String(m, 'UTF-16') } as Function<byte[], Object>,
        'application/json'          : { byte[] m -> JsonParser.newInstance().parse(m) } as Function<byte[], Object>,
        'text/json'                 : { byte[] m -> JsonParser.newInstance().parse(m) } as Function<byte[], Object>
    ]
    private Object body

    ErsatzRequestWithContent(final String method, final String path) {
        super(method, path)
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    RequestWithContent body(Object body) {
        this.body = body
        this
    }

    @Override
    RequestWithContent contentType(final String contentType) {
        header(CONTENT_TYPE_HEADER, contentType)
        this
    }

    @Override
    RequestWithContent converter(final String contentType, final Function<byte[], Object> converter) {
        converters[contentType] = converter
        this
    }

    Object getBody() {
        body
    }

    boolean matches(final HttpServerExchange exchange) {
        super.matches(exchange) && bodyEquals(body, converters[getHeader(CONTENT_TYPE_HEADER) ?: DEFAULT_CONTENT_TYPE])
    }
}
