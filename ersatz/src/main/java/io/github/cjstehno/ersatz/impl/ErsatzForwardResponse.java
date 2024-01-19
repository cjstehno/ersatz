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
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is an empty implementation that does not allow configuration - it denotes that the response is to contain the
 * response from a request made to a forwarded.
 */
@RequiredArgsConstructor
public class ErsatzForwardResponse implements Response {

    @Getter private final URI proxyTargetUri;

    private static final String EXCEPTION_MESSAGE = "A forward response is not configurable.";

    @Override public Response body(final Object content) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response header(final String name, final String... value) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response header(final String name, final List<String> values) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response headers(final Map<String, Object> headers) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response cookie(final String name, final String value) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response cookie(final String name, final Cookie cookie) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response cookies(final Map<String, String> cookies) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public String getContentType() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response code(final int code) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response delay(final long time) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public long getDelay() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response chunked(final Consumer<ChunkingConfig> config) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Map<String, List<String>> getHeaders() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Map<String, Object> getCookies() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public byte[] getContent() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Integer getCode() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response encoder(final String contentType, final Class objectType, final Function<Object, byte[]> encoder) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override public Response encoders(final ResponseEncoders encoders) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }
}
