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

import com.stehno.ersatz.*
import org.hamcrest.Matcher

import java.util.function.BiFunction

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith

/**
 * Ersatz implementation of a <code>Request</code> with request body content.
 */
@SuppressWarnings('ConfusingMethodName')
class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    private final RequestDecoders localDecoders = new RequestDecoders()
    private final DecoderChain decoderChain = new DecoderChain(localDecoders)

    /**
     * Creates a request with the specified method and path.
     *
     * @param method the request method
     * @param pathMatcher the request path matcher
     */
    ErsatzRequestWithContent(
        final HttpMethod method,
        final Matcher<String> pathMatcher,
        final RequestDecoders globalDecoders = null,
        final ResponseEncoders globalEncoders = null
    ) {
        super(method, pathMatcher, globalEncoders)

        if (globalDecoders) {
            decoderChain.last globalDecoders
        }
    }

    @Override
    RequestWithContent body(final Matcher<Object> bodyMatcher, final String contentType) {
        addMatcher RequestMatcher.contentType(startsWith(contentType))
        addMatcher RequestMatcher.body(decoderChain, contentType, bodyMatcher)
        this
    }

    @Override
    RequestWithContent body(final Object obj, final String contentType) {
        body equalTo(obj), contentType
    }

    @Override
    RequestWithContent body(final Matcher<Object> bodyMatcher, final ContentType contentType) {
        body bodyMatcher, contentType.value
    }

    @Override
    RequestWithContent body(final Object obj, final ContentType contentType) {
        body obj, contentType.value
    }

    @Override
    RequestWithContent decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        localDecoders.register(contentType, decoder)
        this
    }

    @Override
    RequestWithContent decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        localDecoders.register(contentType, decoder)
        this
    }

    @Override
    RequestWithContent decoders(final RequestDecoders requestDecoders) {
        decoderChain.second(requestDecoders)
        this
    }
}

