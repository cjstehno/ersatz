/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.ChunkingConfig;
import io.github.cjstehno.ersatz.cfg.Response;
import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.encdec.EncoderChain;
import io.github.cjstehno.ersatz.encdec.ErsatzMultipartResponseContent;
import io.github.cjstehno.ersatz.encdec.MultipartResponseContent;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import io.github.cjstehno.ersatz.util.StatusCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.cjstehno.ersatz.cfg.ContentType.CONTENT_TYPE_HEADER;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableMap;

/**
 * Implementation of the <code>Response</code> interface.
 */
@Slf4j
public class ErsatzResponse implements Response {

    private final ResponseEncoders localEncoders = new ResponseEncoders();
    private final EncoderChain encoderChain;

    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private final Map<String, Object> cookies = new LinkedHashMap<>();
    private ChunkingConfigImpl chunkingConfig;
    private final AtomicReference<byte[]> cachedContent = new AtomicReference<>();
    private Object content;
    private Integer code = StatusCode.OK.getValue();
    private long delayTime;

    /**
     * Creates a new response implementation with the provided parameters.
     *
     * @param globalEncoders the configured global encoders
     */
    public ErsatzResponse(final ResponseEncoders globalEncoders) {
        this.encoderChain = new EncoderChain(globalEncoders, localEncoders);
    }

    /**
     * Used to retrieve the chunking configuration, if any.
     *
     * @return the chunking configuration
     */
    public ChunkingConfigImpl getChunkingConfig() {
        return chunkingConfig;
    }

    @Override
    public Response body(final Object content) {
        this.content = content;

        if (content instanceof MultipartResponseContent) {
            final var multipartContent = (ErsatzMultipartResponseContent) content;

            // apply the configured encoders
            encoderChain.items().forEach(multipartContent::encoders);

            // apply the content type (with boundary)
            contentType(multipartContent.getContentType());
        }

        return this;
    }

    @Override
    public Response header(final String name, final String... value) {
        final List<String> list = headers.computeIfAbsent(name, s -> new LinkedList<>());
        list.addAll(Arrays.asList(value));
        return this;
    }

    @Override
    public Response header(final String name, final List<String> values) {
        final List<String> list = headers.computeIfAbsent(name, s -> new LinkedList<>());
        list.addAll(values);
        return this;
    }

    @Override
    public Response headers(final Map<String, Object> headers) {
        headers.forEach((k, v) -> {
            if (v instanceof List) {
                header(k, (List<String>) v);
            } else {
                header(k, v.toString());
            }
        });
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
    public String getContentType() {
        final var type = headers.get(CONTENT_TYPE_HEADER);
        return type != null ? String.join(",", type) : TEXT_PLAIN.getValue();
    }

    @Override public Response code(final int code) {
        this.code = code;
        return this;
    }

    @Override
    public Response delay(final long time) {
        this.delayTime = time;
        return this;
    }

    @Override
    public long getDelay() {
        return this.delayTime;
    }

    @Override
    public Response chunked(final Consumer<ChunkingConfig> config) {
        chunkingConfig = new ChunkingConfigImpl();
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
    public byte[] getContent() {
        if (content != null) {
            if (cachedContent.get() == null) {
                val encoder = encoderChain.resolve(getContentType(), content.getClass());
                if (encoder != null) {
                    log.debug("Found encoder ({}) for content ({}).", encoder, content.getClass().getSimpleName());
                    cachedContent.set(encoder.apply(content));

                } else if (content instanceof byte[]) {
                    log.warn("No encoder configured for byte[] of type ({}) - returning raw bytes.", getContentType());
                    cachedContent.set((byte[]) content);

                } else {
                    log.warn(
                        "No encoder configured for content ({}) of type ({}) - returning string bytes.",
                        content.getClass().getSimpleName(), getContentType()
                    );
                    cachedContent.set(content.toString().getBytes(UTF_8));
                }
            }

            return cachedContent.get();
        }

        log.trace("No response content - returning empty array.");

        return new byte[0];
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public Response encoder(final String contentType, final Class objectType, final Function<Object, byte[]> encoder) {
        localEncoders.register(contentType, objectType, encoder);
        return this;
    }

    @Override
    public Response encoders(final ResponseEncoders encoders) {
        localEncoders.merge(encoders);
        return this;
    }
}
