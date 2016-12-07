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
import com.stehno.ersatz.RequestWithContent
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import java.util.function.Function

import static com.stehno.ersatz.Conditions.bodyEquals

/**
 * Ersatz implementation of a <code>Request</code> with request body content.
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
        'application/json'          : { byte[] m -> new JsonSlurper().parse(m) } as Function<byte[], Object>,
        'text/json'                 : { byte[] m -> new JsonSlurper().parse(m) } as Function<byte[], Object>
    ]
    private Object body

    /**
     * Creates a request with the specified method and path.
     *
     * @param method the request method
     * @param path the request path
     */
    ErsatzRequestWithContent(final String method, final String path) {
        super(method, path)
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    RequestWithContent body(final Object body) {
        this.body = body
        this
    }

    @Override @SuppressWarnings('ConfusingMethodName')
    RequestWithContent body(final Object body, final String contentType) {
        this.body(body)
        this.contentType(contentType)
    }

    @Override
    RequestWithContent contentType(final String contentType) {
        header(CONTENT_TYPE_HEADER, contentType)
        this
    }

    String getContentType() {
        getHeader(CONTENT_TYPE_HEADER)
    }

    @Override
    RequestWithContent converter(final String contentType, final Function<byte[], Object> converter) {
        converters[contentType] = converter
        this
    }

    /**
     * Used to retrieve the configured body content.
     *
     * @return the configured body content
     */
    Object getBody() {
        body
    }

    /**
     * Used to determine whether or not the incoming client request matches this configured request. If there are configured <code>conditions</code>,
     * they will override the default match conditions, and only those configured conditions will be applied. The default conditions may be added
     * back in using the <code>Conditions</code> functions.
     *
     * The default match criteria are:
     *
     * <ul>
     *  <li>The request methods must match.</li>
     *  <li>The request paths must match.</li>
     *  <li>The request query parameters must match (inclusive).</li>
     *  <li>The incoming request headers must contain all of the configured headers (non-inclusive).</li>
     *  <li>The incoming request cookies must contain all of the configured cookies (non-inclusive).</li>
     *  <li>The incoming request body content must match the configured body content (after conversion).</li>
     * </ul>
     *
     * @param clientRequest the incoming client request
     * @return true if the incoming request matches the configured request
     */
    boolean matches(final ClientRequest clientRequest) {
        boolean matches = super.matches(clientRequest)
        conditions ? matches : matches && bodyEquals(body, findConverter(contentType)).apply(clientRequest)
    }

    private Function<byte[], Object> findConverter(final String contentType) {
        converters[contentType ?: DEFAULT_CONTENT_TYPE] ?: converters[DEFAULT_CONTENT_TYPE]
    }

    @Override String toString() {
        "${super.toString()}: $body"
    }
}
