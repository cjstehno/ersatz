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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.core.IsIterableContaining;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

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

    public ErsatzRequestWithContent(final HttpMethod method, final Matcher<String> pathMatcher) {
        this(method, pathMatcher, null, null);
    }

    @Override
    public RequestWithContent body(final Matcher<Object> bodyMatcher, final String contentType) {
        addMatcher(RequestMatcher.contentType(startsWith(contentType)));
        addMatcher(RequestMatcher.body(decoderChain, contentType, bodyMatcher));
        return this;
    }

    @Override
    public RequestWithContent body(final Object obj, final String contentType) {
      return  body (equalTo (obj), contentType);
    }

    @Override
    public RequestWithContent body(final Matcher<Object> bodyMatcher, final ContentType contentType) {
        return body( bodyMatcher, contentType.getValue());
    }

    @Override
    public RequestWithContent body(final Object obj, final ContentType contentType) {
        return body( obj, contentType.getValue());
    }

    @Override
    public RequestWithContent decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        localDecoders.register(contentType, decoder);
        return this;
    }

    @Override
    public RequestWithContent decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        localDecoders.register(contentType, decoder);
        return this;
    }

    @Override
    public RequestWithContent param(String name, String value) {
        return param( name, value !=null ? IsIterableContaining.hasItem(value) : IsIterableContaining.hasItem(""));
    }

    @Override
    public RequestWithContent param(String name, Iterable<? super String> values) {
        final var matchers = new LinkedList<Matcher<? super String>>();
        values.forEach(v -> matchers.add(equalTo(v)));

        return param(name, new StringIterableMatcher(matchers));
    }

    // FIXME: merge with QueryMatcher to reduce duplication
    private static class StringIterableMatcher extends BaseMatcher<Iterable<? super String>> {

        private final Collection<Matcher<? super String>> matchers;

        StringIterableMatcher(final Collection<Matcher<? super String>> matchers) {
            this.matchers = matchers;
        }

        @Override public boolean matches(final Object item) {
            return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers).matches(item);
        }

        @Override public void describeTo(final Description description) {
            // fIXME: impl
        }
    }

    @Override
    public RequestWithContent param(String name, Matcher<Iterable<? super String>> matchers) {
        addMatcher( RequestMatcher.param(name, matchers));
        return this;
    }
}

