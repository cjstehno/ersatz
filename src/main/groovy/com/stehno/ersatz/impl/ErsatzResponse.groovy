/*
 * Copyright (C) 2017 Christopher J. Stehno
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
import com.stehno.ersatz.HttpMethod
import com.stehno.ersatz.MultipartResponseContent
import com.stehno.ersatz.Response
import com.stehno.ersatz.ResponseEncoders
import groovy.transform.CompileStatic

import java.util.function.Function

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER
import static com.stehno.ersatz.ContentType.TEXT_PLAIN

/**
 * Implementation of the <code>Response</code> interface.
 */
@CompileStatic @SuppressWarnings('ConfusingMethodName')
class ErsatzResponse implements Response {

    private static final String ALLOW_HEADER = 'Allow'
    private final boolean empty
    private final ResponseEncoders localEncoders = new ResponseEncoders()
    private final EncoderChain encoderChain = new EncoderChain(localEncoders)

    ErsatzResponse(final boolean empty, final ResponseEncoders globalEncoders = null) {
        this.empty = empty

        if (globalEncoders) {
            encoderChain.last(globalEncoders)
        }
    }

    private final Map<String, String> headers = [:]
    private final Map<String, String> cookies = [:]
    private Object content
    private Integer code = 200

    @Override
    Response content(final Object content) {
        if (empty) {
            throw new IllegalArgumentException('The response is configured as EMPTY and cannot have content.')
        }

        this.content = content

        if (content instanceof MultipartResponseContent) {
            // apply the configured encoders
            encoderChain.items().each { ResponseEncoders re ->
                (content as ErsatzMultipartResponseContent).encoders(re)
            }

            // apply the content type (with boundary)
            contentType((content as ErsatzMultipartResponseContent).contentType)
        }

        this
    }

    @Override
    Response content(final Object content, final String contentType) {
        this.content(content)
        this.contentType(contentType)
    }

    @Override
    Response content(final Object content, final ContentType contentType) {
        this.content(content, contentType.value)
    }

    @Override
    Response header(final String name, final String value) {
        headers[name] = value
        this
    }

    @Override
    Response headers(final Map<String, String> headers) {
        this.headers.putAll(headers)
        this
    }

    @Override
    Response allows(final HttpMethod... methods) {
        String values = methods.collect { HttpMethod m -> m.value }.join(',')

        headers[ALLOW_HEADER] = (headers.containsKey(ALLOW_HEADER) ? "${headers[ALLOW_HEADER]},$values" : values) as String

        this
    }

    @Override
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
        headers[CONTENT_TYPE_HEADER] ?: TEXT_PLAIN.value
    }

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
    String getContent() {
        if (content != null) {
            return encoderChain.resolve(contentType, content.class)?.apply(content) ?: (content as String)

        }
        return ''
    }

    @Override
    Integer getCode() {
        code
    }

    @Override
    Response encoder(final String contentType, final Class objectType, final Function<Object, String> encoder) {
        localEncoders.register(contentType, objectType, encoder)
        this
    }

    @Override
    Response encoder(final ContentType contentType, final Class objectType, final Function<Object, String> encoder) {
        localEncoders.register(contentType, objectType, encoder)
        this
    }

    @Override
    Response encoders(final ResponseEncoders encoders) {
        encoderChain.second(encoders)
        this
    }
}
