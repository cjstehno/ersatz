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

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.Cookie
import com.stehno.ersatz.DecodingContext
import com.stehno.ersatz.HttpMethod
import groovy.transform.TupleConstructor
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER
import static com.stehno.ersatz.ErsatzMatchers.collectionContainsMatch

/**
 * Request-specific wrapper around hamcrest matchers to provide property-based matching based on request attributes.
 */
@TupleConstructor
class RequestMatcher extends BaseMatcher<ClientRequest> {

    /**
     * The wrapped hamcrest matcher.
     */
    Matcher<Object> matcher

    /**
     * The closure used to extract the matching data from the client request.
     */
    Closure<Object> getter

    /**
     * The matcher description for display purposes (used to prefix the embedded matcher).
     */
    String description

    /**
     * Creates a request matcher for the protocol property value.
     *
     * @param m the hamcrest matcher for the protocol property
     * @return a configured RequestMatcher
     */
    static RequestMatcher protocol(final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.protocol }, 'Protocol matches ')
    }

    /**
     * Creates a request matcher for the method property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher method(final Matcher<HttpMethod> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.method }, 'HTTP method matches ')
    }

    /**
     * Creates a request matcher for the path property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher path(final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.path }, 'Path matches ')
    }

    /**
     * Creates a request matcher for a request header.
     *
     * @param name the header name
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher header(final String name, final Matcher<Iterable<String>> m) {
        new RequestMatcher(m, { ClientRequest cr ->
            cr.headers.get(name)?.toList()
        }, "Header $name matches ")
    }

    /**
     * Creates a request matcher for a query parameter.
     *
     * @param name the query parameter name
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher query(final String name, final Matcher<Iterable<String>> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.queryParams.get(name) as List }, "Query string $name matches ")
    }

    /**
     * Creates a request matcher for a cookie.
     *
     * @param name the cookie name
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher cookie(final String name, final Matcher<Cookie> m) {
        new RequestMatcher(m, { ClientRequest cr ->
            io.undertow.server.handlers.Cookie undertowCookie = cr.cookies.get(name)
            return undertowCookie ? bake(undertowCookie) : null
        }, "Cookie $name matches ")
    }

    private static Cookie bake(final io.undertow.server.handlers.Cookie cookie) {
        new Cookie(
            value: cookie.value,
            comment: cookie.comment,
            domain: cookie.domain,
            path: cookie.path,
            maxAge: cookie.maxAge,
            httpOnly: cookie.httpOnly,
            secure: cookie.secure,
            version: cookie.version
        )
    }

    static RequestMatcher cookies(final Matcher<Map<String, Cookie>> matcher) {
        new RequestMatcher(matcher, { ClientRequest cr ->
            cr.cookies.collectEntries { name, cookie ->
                [name, bake(cookie)]
            }
        }, 'Cookies match ')
    }

    /**
     * Creates a request matcher for request body content.
     *
     * @param decoders the available request decoders
     * @param contentType the request content-type
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher body(final DecoderChain decoderChain, final String contentType, final Matcher<Object> m) {
        new RequestMatcher(m, { ClientRequest cr ->
            decoderChain.resolve(contentType)?.apply(
                cr.body,
                new DecodingContext(cr.contentLength, cr.contentType, cr.characterEncoding, decoderChain)
            )
        }, "Body ($contentType) matches ")
    }

    /**
     * Creates a request matcher for the request content-type property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher contentType(final Matcher<String> m) {
        header(CONTENT_TYPE_HEADER, collectionContainsMatch(m))
    }

    /**
     * Performs the hamcrest matching using the wrapped matcher and property extractor.
     *
     * @param cr the client request
     * @return true if the matcher is successful
     */
    @Override
    boolean matches(final Object item) {
        matcher.matches(getter.call(item as ClientRequest))
    }

    @Override
    void describeTo(final Description description) {
        if (this.description) {
            description.appendText(this.description)
        }
        matcher.describeTo(description)
    }
}