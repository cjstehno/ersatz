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

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.server.ClientRequest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Hamcrest Matcher implementation for matching an incoming HTTP request with the configured matcher conditions.
 */
public class ProxyRequestMatcher extends BaseMatcher<ClientRequest> {

    private final Matcher<HttpMethod> methodMatcher;
    private final Matcher<String> pathMatcher;
    private int count = 0;

    ProxyRequestMatcher(final Matcher<HttpMethod> methodMatcher, final Matcher<String> pathMatcher) {
        this.methodMatcher = methodMatcher;
        this.pathMatcher = pathMatcher;
    }

    /**
     * Used to retrieve the number of times this matcher has been matched.
     *
     * @return the number of matches
     */
    public int getMatchCount() {
        return count;
    }

    @Override
    public boolean matches(final Object item) {
        final var clientRequest = (ClientRequest) item;

        boolean matched = methodMatcher.matches(clientRequest.getMethod()) && pathMatcher.matches(clientRequest.getPath());

        if (matched) {
            count++;
        }

        return matched;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("A proxied request for ");
        description.appendDescriptionOf(methodMatcher);
        description.appendText(": ");
        description.appendDescriptionOf(pathMatcher);
    }
}