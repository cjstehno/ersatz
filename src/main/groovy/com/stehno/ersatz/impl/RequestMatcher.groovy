/*
 * Copyright (C) 2016 Christopher J. Stehno
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
import com.stehno.ersatz.DecodingContext
import com.stehno.ersatz.RequestDecoders
import groovy.transform.TupleConstructor
import org.hamcrest.Matcher

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER

/**
 * Request-specific wrapper around hamcrest matchers to provide property-based matching based on request attributes.
 */
@TupleConstructor
class RequestMatcher {

    /**
     * The wrapped hamcrest matcher.
     */
    Matcher<Object> matcher

    /**
     * The closure used to extract the matching data from the client request.
     */
    Closure<Object> getter

    /**
     * Creates a request matcher for the method property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher method(final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.method })
    }

    /**
     * Creates a request matcher for the path property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher path(final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.path })
    }

    /**
     * Creates a request matcher for a request header.
     *
     * @param name the header name
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher header(final String name, final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.headers.getFirst(name) })
    }

    /**
     * Creates a request matcher for a query parameter.
     *
     * @param name the query parameter name
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher query(final String name, final Matcher<Iterable<String>> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.queryParams.get(name) as List })
    }

    /**
     * Creates a request matcher for a cookie value.
     *
     * @param name the cookie name
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher cookie(final String name, final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.cookies.get(name)?.value })
    }

    /**
     * Creates a request matcher for request body content.
     *
     * @param decoders the available request decoders
     * @param contentType the request content-type
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher body(final RequestDecoders decoders, final String contentType, final Matcher<Object> m) {
        new RequestMatcher(m, { ClientRequest cr ->
            decoders.findDecoder(contentType)?.apply(cr.body, new DecodingContext(cr.contentLength, cr.contentType, cr.characterEncoding, decoders))
        })
    }

    /**
     * Creates a request matcher for the request content-type property.
     *
     * @param m the hamcrest matcher to be wrapped
     * @return a configured RequestMatcher
     */
    static RequestMatcher contentType(final Matcher<String> m) {
        header(CONTENT_TYPE_HEADER, m)
    }

    /**
     * Performs the hamcrest matching using the wrapped matcher and property extractor.
     *
     * @param cr the client request
     * @return true if the matcher is successful
     */
    boolean matches(final ClientRequest cr) {
        matcher.matches(getter.call(cr))
    }
}