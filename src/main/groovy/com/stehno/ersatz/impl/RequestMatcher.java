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

import com.stehno.ersatz.ClientRequest;
import com.stehno.ersatz.Cookie;
import com.stehno.ersatz.DecodingContext;
import com.stehno.ersatz.HttpMethod;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsIterableContaining;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER;
import static io.undertow.util.QueryParameterUtils.parseQueryString;

/**
 * Request-specific wrapper around hamcrest matchers to provide property-based matching based on request attributes.
 */
public class RequestMatcher extends BaseMatcher<ClientRequest> {

    private final Matcher<?> matcher;
    private final Function<ClientRequest, Object> getter;
    private final String description;

    private RequestMatcher(Matcher<?> matcher, Function<ClientRequest, Object> getter, String description) {
        this.matcher = matcher;
        this.getter = getter;
        this.description = description;
    }

    public Matcher<?> getMatcher() {
        return matcher;
    }

    /**
     * Creates a request matcher for the protocol property value.
     *
     * @param m the hamcrest matcher for the protocol property
     * @return a configured RequestMatcher
     */
    public static RequestMatcher protocol(final Matcher<String> m) {
        return new RequestMatcher(m, ClientRequest::getProtocol, "Protocol matches ");
    }

    /**
     * Creates a request matcher for the method property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    public static RequestMatcher method(final Matcher<HttpMethod> m) {
        return new RequestMatcher(m, ClientRequest::getMethod, "HTTP method matches ");
    }

    /**
     * Creates a request matcher for the path property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    public static RequestMatcher path(final Matcher<String> m) {
        return new RequestMatcher(m, ClientRequest::getPath, "Path matches ");
    }

    /**
     * Creates a request matcher for a request header.
     *
     * @param name the header name
     * @param m    the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    public static RequestMatcher header(final String name, final Matcher<Iterable<? super String>> m) {
        return new RequestMatcher(
            m,
            cr -> cr.getHeaders().contains(name) ? Arrays.asList(cr.getHeaders().get(name).toArray()) : null,
            "Header " + name + " matches "
        );
    }

    /**
     * Creates a request matcher for a query parameter.
     *
     * @param name the query parameter name
     * @param m    the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    public static RequestMatcher query(final String name, final Matcher<Iterable<? super String>> m) {
        return new RequestMatcher(
            m,
            cr -> {
                final var qs = cr.getQueryParams().get(name);
                if( qs  != null){
                    return new ArrayDeque<>(Arrays.asList(qs.toArray(new String[0])));
                } else {
                    return null;
                }
            },
            "Query string " + name + " matches "
        );
    }

    /**
     * Creates a request matcher for parameters specified within the request body.
     *
     * @param name the name of the parameter
     * @param m    the matchers
     * @return the configured RequestMatcher
     */
    public static RequestMatcher param(final String name, final Matcher<Iterable<? super String>> m) {
        return new RequestMatcher(
            m,
            cr -> {
                final var qs = parseQueryString(new String(cr.getBody() != null ? cr.getBody() : new byte[0], StandardCharsets.UTF_8), StandardCharsets.UTF_8.displayName());
                return Arrays.asList(qs.containsKey(name) ? qs.get(name).toArray() : new String[0]);
            },
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
    public static RequestMatcher cookie(final String name, final Matcher<Cookie> m) {
        return new RequestMatcher(
            m,
            cr -> {
                final var undertowCookie = cr.getCookies().get(name);
                return undertowCookie != null ? bake(undertowCookie) : null;
            },
            "Cookie " + name + " matches "
        );
    }

    private static Cookie bake(final io.undertow.server.handlers.Cookie cookie) {
        final var localCookie = new Cookie();
        localCookie.value(cookie.getValue());
        localCookie.comment(cookie.getComment());
        localCookie.domain(cookie.getDomain());
        localCookie.path(cookie.getPath());
        localCookie.maxAge(cookie.getMaxAge());
        localCookie.httpOnly(cookie.isHttpOnly());
        localCookie.secure(cookie.isSecure());
        localCookie.version(cookie.getVersion());
        return localCookie;
    }

    public static RequestMatcher cookies(final Matcher<Map<String, Cookie>> matcher) {
        return new RequestMatcher(
            matcher,
            cr -> {
                final var map = new LinkedHashMap<String, Cookie>();
                cr.getCookies().forEach((name, cookie) -> map.put(name, bake(cookie)));
                return map;
            },
            "Cookies match "
        );
    }

    /**
     * Creates a request matcher for request body content.
     *
     * @param decoderChain    the available request decoders
     * @param contentType the request content-type
     * @param m           the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    public static RequestMatcher body(final DecoderChain decoderChain, final String contentType, final Matcher<Object> m) {
        return new RequestMatcher(
            m,
            cr -> {
                final var decoder = decoderChain.resolve(contentType);
                if( decoder != null){
                    return decoder.apply(cr.getBody(), new DecodingContext(cr.getContentLength(), cr.getContentType(), cr.getCharacterEncoding(), decoderChain));
                } else {
                    return null;
                }
            },
            "Body ($contentType) matches "
        );
    }

    /**
     * Creates a request matcher for the request content-type property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    public static RequestMatcher contentType(final Matcher<String> m) {
        return header(CONTENT_TYPE_HEADER, IsIterableContaining.hasItem(m));
    }

    /**
     * Creates a matcher based on the <code>ClientRequest</code> object.
     *
     * @param crm the matcher
     * @return a configured RequestMatcher
     */
    public static RequestMatcher matcher(final Matcher<ClientRequest> crm) {
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
        return matcher.matches(getter.apply((ClientRequest)item));
    }

    @Override
    public void describeTo(final Description description) {
        if (this.description != null) {
            description.appendText(this.description);
        }
        matcher.describeTo(description);
    }
}