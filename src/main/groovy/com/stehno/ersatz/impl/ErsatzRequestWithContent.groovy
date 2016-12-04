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
import io.undertow.io.Receiver
import io.undertow.server.HttpServerExchange

import java.util.function.Function

/**
 * Ersatz implementation of a <code>Request</code> with body content.
 */
@CompileStatic
class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    private final Map<String, Function<byte[], Object>> converters = [
        'text/plain'                : { byte[] m -> body == new String(m, 'UTF-8') } as Function<byte[], Object>,
        'text/plain; charset=utf-8' : { byte[] m -> body == new String(m, 'UTF-8') } as Function<byte[], Object>,
        'text/plain; charset=utf-16': { byte[] m -> body == new String(m, 'UTF-16') } as Function<byte[], Object>
    ]
    private Object body

    ErsatzRequestWithContent(final String method, final String path) {
        super(method, path)
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    Request body(Object body) {
        this.body = body
        this
    }

    Request converter(final String contentType, final Function<byte[], Object> converter) {
        converters[contentType] = converter
        this
    }

    Object getBody() {
        body
    }

    boolean matches(final HttpServerExchange exchange) {
        super.matches(exchange) && matchesBody(exchange)
    }

    private boolean matchesBody(final HttpServerExchange exchange) {
        boolean match = false

        exchange.requestReceiver.receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            void handle(final HttpServerExchange exch, byte[] message) {
                match = converters[getHeader('Content-Type') ?: 'text/plain; charset=utf-8']
            }
        })

        match
    }
}
