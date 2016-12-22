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
import groovy.transform.Immutable
import org.hamcrest.Matcher

/**
 * Request-specific wrapper around hamcrest matchers to provide property-based matching based on request attributes.
 */
@Immutable(knownImmutableClasses = [Matcher, Closure])
class RequestMatcher {

    Matcher<Object> matcher
    Closure<Object> getter

    // FIXME: document these
    static RequestMatcher method(final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.method })
    }

    static RequestMatcher path(final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.path })
    }

    static RequestMatcher header(final String name, final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.headers.getFirst(name) })
    }

    static RequestMatcher query(final String name, final Matcher<Iterable<String>> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.queryParams.get(name) as List })
    }

    static RequestMatcher cookie(final String name, final Matcher<String> m) {
        new RequestMatcher(m, { ClientRequest cr -> cr.cookies.get(name)?.value })
    }

    static RequestMatcher body(final RequestDecoders decoders, final String contentType, final Matcher<Object> m) {
        new RequestMatcher(m, { ClientRequest cr ->
            decoders.findDecoder(contentType)?.apply(cr.body, new DecodingContext(cr.contentLength, cr.contentType, cr.characterEncoding, decoders))
        })
    }

    static RequestMatcher contentType(final Matcher<String> m) {
        header('Content-Type', m)
    }

    boolean matches(final ClientRequest cr) {
        matcher.matches(getter.call(cr))
    }
}