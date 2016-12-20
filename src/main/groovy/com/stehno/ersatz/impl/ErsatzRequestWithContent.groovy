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
import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Converters
import com.stehno.ersatz.RequestWithContent

import java.util.function.Function

import static com.stehno.ersatz.Conditions.bodyEquals
import static com.stehno.ersatz.ContentType.*

/**
 * Ersatz implementation of a <code>Request</code> with request body content.
 */
class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    public static final String CONTENT_TYPE_HEADER = 'Content-Type'

    private final RequestContentConverters converters = new RequestContentConverters({
        register TEXT_PLAIN, Converters.utf8String
        register APPLICATION_JSON, Converters.parseJson
        register TEXT_JSON, Converters.parseJson
        register APPLICATION_URLENCODED, Converters.urlEncoded
    })

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

    @Override @SuppressWarnings('ConfusingMethodName')
    RequestWithContent body(final Object body, final ContentType contentType) {
        this.body(body)
        this.contentType(contentType)
    }

    @Override
    RequestWithContent contentType(final String contentType) {
        header(CONTENT_TYPE_HEADER, contentType)
        this
    }

    @Override
    RequestWithContent contentType(final ContentType contentType) {
        this.contentType(contentType.value)
    }

    String getContentType() {
        getHeader(CONTENT_TYPE_HEADER)
    }

    @Override
    RequestWithContent converter(final String contentType, final Function<byte[], Object> converter) {
        converters.register(contentType, converter)
        this
    }

    @Override
    RequestWithContent converter(final ContentType contentType, final Function<byte[], Object> converter) {
        converters.register(contentType, converter)
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
     * they will override the default match conditions (except for path and request method matching, and only those configured conditions will be
     * applied. The default conditions may be added back in using the <code>Conditions</code> functions.
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
        if (conditions) {
            return matches
        }

        return matches &&
            bodyEquals(body, contentType ? converters.findConverter(contentType) : converters.findConverter(TEXT_PLAIN)).apply(clientRequest)
    }

    @Override String toString() {
        "${super.toString()}: $body"
    }
}
