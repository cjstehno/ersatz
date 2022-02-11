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
import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.encdec.DecoderChain;
import io.github.cjstehno.ersatz.encdec.DecodingContext;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static lombok.AccessLevel.PROTECTED;

/**
 * Request-specific wrapper around hamcrest matchers to provide property-based matching based on request attributes.
 */
@RequiredArgsConstructor(access = PROTECTED)
public class RequestMatcher extends BaseMatcher<ClientRequest> {
    // FIXME: this needs to be broken apart into separate matchers to allow more flexibility

    private final Matcher<?> matcher;
    private final Function<ClientRequest, Object> getter;
    private final String description;

    /**
     * Creates a request matcher for the method property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher method(final Matcher<HttpMethod> m) {
        return new RequestMatcher(m, ClientRequest::getMethod, "HTTP method matches ");
    }

    /**
     * Creates a request matcher for parameters specified within the request body.
     *
     * @param name the name of the parameter
     * @param m    the matchers
     * @return the configured RequestMatcher
     */
    static RequestMatcher param(final String name, final Matcher<Iterable<? super String>> m) {
        return new RequestMatcher(
            m,
            cr -> cr.getBodyParameters().getOrDefault(name, new ArrayDeque<>()),
            "Parameter string " + name + " matches"
        );
    }

    /**
     * Creates a request matcher for a cookie.
     *
     * @param name the cookie name
     * @param m    the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher cookie(final String name, final Matcher<Cookie> m) {
        return new RequestMatcher(
            m,
            cr -> cr.getCookies().get(name),
            "Cookie " + name + " matches "
        );
    }

    static RequestMatcher cookies(final Matcher<Map<String, Cookie>> matcher) {
        return new RequestMatcher(
            matcher,
            cr -> {
                final var map = new LinkedHashMap<String, Cookie>();
                cr.getCookies().forEach(map::put);
                return map;
            },
            "Cookies match "
        );
    }

    /**
     * Creates a request matcher for request body content.
     *
     * @param decoderChain the available request decoders
     * @param contentType  the request content-type
     * @param m            the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher body(final DecoderChain decoderChain, final String contentType, final Matcher<Object> m) {
        return new RequestMatcher(
            m,
            cr -> {
                final var decoder = decoderChain.resolve(contentType);
                if (decoder != null) {
                    return decoder.apply(cr.getBody(), new DecodingContext(cr.getContentLength(), cr.getContentType(), cr.getCharacterEncoding(), decoderChain));
                } else {
                    return null;
                }
            },
            "Body (" + contentType + ") matches "
        );
    }

    /**
     * Creates a matcher based on the <code>ClientRequest</code> object.
     *
     * @param crm the matcher
     * @return a configured RequestMatcher
     */
    static RequestMatcher matcher(final Matcher<ClientRequest> crm) {
        return new RequestMatcher(crm, cr -> cr, "Request matches " + crm);
    }

    /**
     * Performs the hamcrest matching using the wrapped matcher and property extractor.
     *
     * @param item the client request
     * @return true if the matcher is successful
     */
    @Override
    public boolean matches(final Object item) {
        return matcher.matches(getter.apply((ClientRequest) item));
    }

    @Override
    public void describeTo(final Description description) {
        if (this.description != null) {
            description.appendText(this.description);
        }
        matcher.describeTo(description);
    }
}