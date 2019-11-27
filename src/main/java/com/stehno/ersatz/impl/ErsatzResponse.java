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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.*;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER;
import static com.stehno.ersatz.ContentType.TEXT_PLAIN;
import static groovy.lang.Closure.DELEGATE_FIRST;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the <code>Response</code> interface.
 */
public class ErsatzResponse implements Response {

    private static final String ALLOW_HEADER = "Allow";
    private final boolean empty;
    private final ResponseEncoders localEncoders = new ResponseEncoders();
    private final EncoderChain encoderChain = new EncoderChain(localEncoders);

    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private final Map<String, Object> cookies = new LinkedHashMap<>();
    private ChunkingConfig chunkingConfig;
    private Object content;
    private Integer code = 200;
    private long delayTime;

    public ErsatzResponse(final boolean empty, final ResponseEncoders globalEncoders) {
        this.empty = empty;

        if (globalEncoders != null ) {
            encoderChain.last(globalEncoders);
        }
    }

    public ErsatzResponse(final boolean empty) {
        this(empty, null);
    }

    @Override public ChunkingConfig getChunkingConfig() {
        return chunkingConfig;
    }

    @Override
    public Response body(Object content) {
        if (empty) {
            throw new IllegalArgumentException("The response is configured as EMPTY and cannot have content.");
        }

        this.content = content;

        if (content instanceof MultipartResponseContent) {
            final var multipartContent = (ErsatzMultipartResponseContent)content;

            // apply the configured encoders
            encoderChain.items().forEach(multipartContent::encoders);

            // apply the content type (with boundary)
            contentType(multipartContent.getContentType());
        }

        return this;
    }

    @Override
    public Response body(Object data, String contentType) {
        body(data);
        return this.contentType(contentType);
    }

    @Override
    public Response body(Object data, ContentType contentType) {
        return body(data, contentType.getValue());
    }

    @Override
    public Response header(final String name, final String... value) {
        final List<String> list = headers.computeIfAbsent(name, s -> new LinkedList<>());
        list.addAll(Arrays.asList(value));
        return this;
    }

    public Response header(final String name, final List<String> values) {
        final List<String> list = headers.computeIfAbsent(name, s -> new LinkedList<>());
        list.addAll(values);
        return this;
    }

    @Override
    public Response headers(final Map<String, Object> headers) {
        headers.forEach((k,v) -> {
            if( v instanceof List){
                header(k, (List<String>)v);
            } else {
                header(k, v.toString());
            }
        });
        return this;
    }

    @Override
    public Response allows(final HttpMethod... methods) {
        header (ALLOW_HEADER, Arrays.stream(methods).map(HttpMethod::getValue).collect(toList()));
        return this;
    }

    @Override
    public Response cookies(final Map<String, String> cookies) {
        this.cookies.putAll(cookies);
        return this;
    }

    @Override
    public Response cookie(final String name, final String value) {
        cookies.put(name, value);
        return this;
    }

    @Override
    public Response cookie(final String name, final Cookie cookie) {
        cookies.put(name, cookie);
        return this;
    }

    @Override
    public Response contentType(final String contentType) {
        header(CONTENT_TYPE_HEADER, contentType);
        return this;
    }

    @Override
    public Response contentType(final ContentType contentType) {
        header(CONTENT_TYPE_HEADER, contentType.getValue());
        return this;
    }

    @Override
    public String getContentType() {
        final var type = headers.get(CONTENT_TYPE_HEADER);
        return type != null ? String.join(",", type) : TEXT_PLAIN.getValue();
    }

    public Response code(final int code) {
        this.code = code;
        return this;
    }

    @Override
    public Response delay(final long time) {
        return delay(time, MILLISECONDS);
    }

    @Override
    public Response delay(final long time, final TimeUnit unit) {
        this.delayTime = MILLISECONDS.convert(time, unit);
        return this;
    }

    @Override
    public Response delay(final String time) {
        this.delayTime = Duration.parse(time).toMillis();
        return this;
    }

    @Override
    public long getDelay() {
        return this.delayTime;
    }

    @Override
    public Response chunked(@DelegatesTo(value = ChunkingConfig.class, strategy = DELEGATE_FIRST) Closure closure) {
        return chunked(ConsumerWithDelegate.create(closure));
    }

    @Override
    public Response chunked(Consumer<ChunkingConfig> config) {
        chunkingConfig = new ChunkingConfig();
        config.accept(chunkingConfig);
        return this;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return unmodifiableMap(headers);
    }

    @Override
    public Map<String, Object> getCookies() {
        return unmodifiableMap(cookies);
    }

    @Override
    public String getContent() {
        if (content != null) {
            final var encoder = encoderChain.resolve(getContentType(), content.getClass());
            return encoder != null ? encoder.apply(content) : (content.toString());
        }
        return "";
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public Response encoder(final String contentType, final Class objectType, final Function<Object, String> encoder) {
        localEncoders.register(contentType, objectType, encoder);
        return this;
    }

    @Override
    public Response encoder(final ContentType contentType, final Class objectType, final Function<Object, String> encoder) {
        localEncoders.register(contentType, objectType, encoder);
        return this;
    }

    @Override
    public Response encoders(final ResponseEncoders encoders) {
        encoderChain.second(encoders);
        return this;
    }
}
