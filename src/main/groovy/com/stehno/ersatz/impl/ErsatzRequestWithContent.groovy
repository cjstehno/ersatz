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

import com.stehno.ersatz.Request
import com.stehno.ersatz.RequestWithContent
import groovy.transform.CompileStatic
import io.undertow.server.HttpServerExchange

/**
 * Created by cjstehno on 12/4/16.
 */
@CompileStatic
class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    private Object body

    ErsatzRequestWithContent(final String path) {
        super(path)
    }

    @Override
    Request body(Object body) {
        this.body = body
        this
    }

    Object getBody() {
        body
    }

    boolean matches(final HttpServerExchange exchange) {
        super.matches(exchange)
//            && matchesBody(exchange) FIXME: body content is not being matched right now - fix this
    }

    private boolean matchesBody(final HttpServerExchange exchange) {
        byte[] content = exchange.startBlocking().inputStream.bytes

        // FIXME: support other content types and pluggable converters

        String contentType = getHeader('Content-Type') ?: 'text/plain'
        if (contentType == 'text/plain; charset=utf-8') {
            return body == new String(content, 'UTF-8')
        } else if (contentType == 'text/plain; charset=utf-16') {
            return body == new String(content, 'UTF-16')
        }

        false
    }
}
