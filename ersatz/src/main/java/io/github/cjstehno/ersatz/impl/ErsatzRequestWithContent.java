/**
 * Copyright (C) 2022 Christopher J. Stehno
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
import io.github.cjstehno.ersatz.match.PathMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsIterableContaining;

import java.util.LinkedList;
import java.util.function.BiFunction;

import static io.github.cjstehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

/**
 * Ersatz implementation of a <code>Request</code> with request body content.
 */
public class ErsatzRequestWithContent extends ErsatzRequest implements RequestWithContent {

    private final RequestDecoders localDecoders = new RequestDecoders();
    private final DecoderChain decoderChain = new DecoderChain(localDecoders);

    /**
     * Creates a request with the specified method and path, along with encoders and decoders.
     *
     * @param method      the request method
     * @param pathMatcher the request path matcher
     * @param globalDecoders the shared global decoders
     * @param globalEncoders the shared global encoders
     */
    public ErsatzRequestWithContent(final HttpMethod method, final PathMatcher pathMatcher, final RequestDecoders globalDecoders, final ResponseEncoders globalEncoders) {
        super(method, pathMatcher, globalEncoders);

        if (globalDecoders != null) {
            decoderChain.last(globalDecoders);
        }
    }

    /**
     * Creates a request with the specified method and path.
     *
     * @param method      the request method
     * @param pathMatcher the request path matcher
     */
    public ErsatzRequestWithContent(final HttpMethod method, final PathMatcher pathMatcher) {
        this(method, pathMatcher, null, null);
    }

    @Override
    public RequestWithContent body(final Matcher<Object> bodyMatcher, final String contentType) {
        addMatcher(RequestMatcher.contentType(startsWith(contentType)));
        addMatcher(RequestMatcher.body(decoderChain, contentType, bodyMatcher));
        return this;
    }

    @Override
    public RequestWithContent decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        localDecoders.register(contentType, decoder);
        return this;
    }

    @Override
    public RequestWithContent param(String name, String value) {
        return param(name, value != null ? IsIterableContaining.hasItem(value) : IsIterableContaining.hasItem(""));
    }

    @Override
    public RequestWithContent param(String name, Iterable<? super String> values) {
        final var matchers = new LinkedList<Matcher<? super String>>();
        values.forEach(v -> matchers.add(equalTo(v)));

        return param(name, stringIterableMatcher(matchers));
    }

    @Override
    public RequestWithContent param(String name, Matcher<Iterable<? super String>> matchers) {
        addMatcher(RequestMatcher.param(name, matchers));
        return this;
    }
}

