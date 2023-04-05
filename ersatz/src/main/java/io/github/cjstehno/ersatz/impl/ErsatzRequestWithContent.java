/**
 * Copyright (C) 2023 Christopher J. Stehno
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

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.cfg.RequestWithContent;
import io.github.cjstehno.ersatz.encdec.DecoderChain;
import io.github.cjstehno.ersatz.encdec.DecodingContext;
import io.github.cjstehno.ersatz.encdec.RequestDecoders;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import io.github.cjstehno.ersatz.match.BodyMatcher;
import io.github.cjstehno.ersatz.match.BodyParamMatcher;
import io.github.cjstehno.ersatz.match.PathMatcher;

import java.util.function.BiFunction;

/**
 * Ersatz implementation of a <code>Request</code> with request body content.
 */
public class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    private final RequestDecoders localDecoders = new RequestDecoders();
    private final DecoderChain decoderChain;

    /**
     * Creates a request with the specified method and path, along with encoders and decoders.
     *
     * @param method      the request method
     * @param pathMatcher the request path matcher
     * @param globalDecoders the shared global decoders
     * @param globalEncoders the shared global encoders
     */
    public ErsatzRequestWithContent(final HttpMethod method, final PathMatcher pathMatcher, final RequestDecoders globalDecoders, final ResponseEncoders globalEncoders) {
        super(method, pathMatcher, globalEncoders, false);
        this.decoderChain = new DecoderChain(globalDecoders, localDecoders);
    }

    @Override public RequestWithContent body(final BodyMatcher bodyMatcher) {
        bodyMatcher.setDecoderChain(decoderChain);
        addMatcher(bodyMatcher);
        return this;
    }

    @Override
    public RequestWithContent decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        localDecoders.register(contentType, decoder);
        return this;
    }

    @Override public RequestWithContent param(final BodyParamMatcher bodyParamMatcher) {
        addMatcher(bodyParamMatcher);
        return this;
    }
}

