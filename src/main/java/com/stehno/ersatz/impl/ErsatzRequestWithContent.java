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
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.*;

/**
 * Ersatz implementation of a <code>Request</code> with request body content.
 */
public class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    private final RequestDecoders localDecoders = new RequestDecoders();
    private final DecoderChain decoderChain = new DecoderChain(localDecoders);

    /**
     * Creates a request with the specified method and path.
     *
     * @param method      the request method
     * @param pathMatcher the request path matcher
     */
    public ErsatzRequestWithContent(final HttpMethod method, final Matcher<String> pathMatcher, final RequestDecoders globalDecoders, final ResponseEncoders globalEncoders) {
        super(method, pathMatcher, globalEncoders);

        if (globalDecoders != null) {
            decoderChain.last(globalDecoders);
        }
    }

    @Override
    public RequestWithContent body(final Matcher<Object> bodyMatcher, final String contentType) {
        addMatcher(RequestMatcher.contentType(startsWith(contentType)));
        addMatcher(RequestMatcher.body(decoderChain, contentType, bodyMatcher));
        return this;
    }

    @Override
    public RequestWithContent body(final Object obj, final String contentType) {
        return body(equalTo(obj), contentType);
    }

    @Override
    public RequestWithContent body(final Matcher<Object> bodyMatcher, final ContentType contentType) {
        return body(bodyMatcher, contentType.getValue());
    }

    @Override
    public RequestWithContent body(final Object obj, final ContentType contentType) {
        return body(obj, contentType.getValue());
    }

    @Override
    public RequestWithContent decoder(String contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        localDecoders.register(contentType, decoder);
        return this;
    }

    @Override
    public RequestWithContent decoder(ContentType contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        return decoder(contentType.getValue(), decoder);
    }

    @Override
    public RequestWithContent decoders(final RequestDecoders requestDecoders) {
        decoderChain.second(requestDecoders);
        return this;
    }

    @Override
    public RequestWithContent param(String name, String value) {
        return param(name, value != null ? contains(value) : contains(""));
    }

    @Override
    public RequestWithContent param(String name, Iterable<String> values) {
        final Collection<Matcher<? super String>> paramMatchers = new ArrayList<>();

        for (final String value : values) {
            paramMatchers.add(equalTo(value));
        }

        return param(name, containsInAnyOrder(paramMatchers));
    }

    @Override public RequestWithContent param(String name, Matcher<Iterable<? extends String>> matchers) {
        addMatcher(RequestMatcher.param(name, matchers));
        return this;
    }
}

