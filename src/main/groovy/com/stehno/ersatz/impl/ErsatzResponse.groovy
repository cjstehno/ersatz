/*
 * Copyright (C) 2019 Christopher J. Stehno
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

import com.stehno.ersatz.ChunkingConfig
import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Cookie
import com.stehno.ersatz.HttpMethod
import com.stehno.ersatz.MultipartResponseContent
import com.stehno.ersatz.Response
import com.stehno.ersatz.ResponseEncoders

import groovy.transform.CompileStatic

import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Function

import space.jasan.support.groovy.closure.ConsumerWithDelegate

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER
import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static groovy.lang.Closure.DELEGATE_FIRST
import static java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Implementation of the <code>Response</code> interface.
 */
@CompileStatic @SuppressWarnings('ConfusingMethodName')
class ErsatzResponse implements Response {

    private static final String ALLOW_HEADER = 'Allow'
    private final boolean empty
    private final ResponseEncoders localEncoders = new ResponseEncoders()
    private final EncoderChain encoderChain = new EncoderChain(localEncoders)

    private final Map<String, List<String>> headers = [:]
    private final Map<String, Object> cookies = [:]
    ChunkingConfig chunkingConfig
    private Object content
    private Integer code = 200
    private long delayTime

    ErsatzResponse(final boolean empty, final ResponseEncoders globalEncoders = null) {
        this.empty = empty

        if (globalEncoders) {
            encoderChain.last(globalEncoders)
        }
    }

    @Override
    Response body(Object content) {
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
    Response body(Object data, String contentType) {
        body(data)
        this.contentType(contentType)
    }

    @Override
    Response body(Object data, ContentType contentType) {
        body(data)
        this.contentType(contentType)
    }

    @Override
    Response header(final String name, final String... value) {
        List<String> list = headers.computeIfAbsent(name) { k -> [] }
        value.each { String v ->
            list << v
        }
        this
    }

    Response header(final String name, final List<String> values) {
        List<String> list = headers.computeIfAbsent(name) { k -> [] }
        list.addAll(values)
        this
    }

    @Override
    Response headers(final Map<String, Object> headers) {
        headers.each { k, v ->
            if (v instanceof List) {
                header k, (v as List<String>)
            } else {
                header k, (v as String)
            }
        }
        this
    }

    @Override
    Response allows(final HttpMethod... methods) {
        header ALLOW_HEADER, methods*.value
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
    Response cookie(final String name, final Cookie cookie) {
        cookies[name] = cookie
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
        headers[CONTENT_TYPE_HEADER]?.join(',') ?: TEXT_PLAIN.value
    }

    Response code(final int code) {
        this.code = code
        this
    }

    @Override
    Response delay(final long time, final TimeUnit unit = MILLISECONDS) {
        this.delayTime = MILLISECONDS.convert(time, unit)
        this
    }

    @Override
    Response delay(final String time) {
        this.delayTime = Duration.parse(time).toMillis()
        this
    }

    @Override
    long getDelay() {
        this.delayTime
    }

    @Override
    Response chunked(@DelegatesTo(value = ChunkingConfig, strategy = DELEGATE_FIRST) Closure closure) {
        chunked(ConsumerWithDelegate.create(closure))
    }

    @Override
    Response chunked(Consumer<ChunkingConfig> config) {
        chunkingConfig = new ChunkingConfig()
        config.accept(chunkingConfig)
        this
    }

    @Override
    Map<String, List<String>> getHeaders() {
        headers.asImmutable()
    }

    @Override
    Map<String, Object> getCookies() {
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
