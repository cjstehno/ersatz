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
package com.stehno.ersatz.proxy.impl

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.HttpMethod
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 * Hamcrest Matcher implementation for matching an incoming HTTP request with the configured matcher conditions.
 */
@CompileStatic @TupleConstructor
class ProxyRequestMatcher extends BaseMatcher<ClientRequest> {

    /**
     * The configured HTTP method matcher.
     */
    final Matcher<HttpMethod> methodMatcher

    /**
     * The configured path matcher.
     */
    final Matcher<String> pathMatcher

    private int count = 0

    /**
     * Used to retrieve the number of times this matcher has been matched.
     *
     * @return the number of matches
     */
    int getMatchCount() {
        count
    }

    @Override
    boolean matches(final Object item) {
        ClientRequest clientRequest = item as ClientRequest
        boolean matched = methodMatcher.matches(clientRequest.method) && pathMatcher.matches(clientRequest.path)

        if (matched) {
            count++
        }

        matched
    }

    @Override
    void describeTo(final Description description) {
        description.appendText('A proxied request for ')
        description.appendDescriptionOf(methodMatcher)
        description.appendText(': ')
        description.appendDescriptionOf(pathMatcher)
    }
}