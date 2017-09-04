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

import com.stehno.ersatz.HttpMethod
import com.stehno.ersatz.proxy.ProxyExpectations
import groovy.transform.CompileStatic
import org.hamcrest.Matcher
import org.hamcrest.Matchers

/**
 * Implementation of the ProxyExpectations interface providing configuration of the expected requests to the proxy server.
 */
@CompileStatic @SuppressWarnings('MethodCount')
class ProxyExpectationsImpl implements ProxyExpectations {

    /**
     * Used to retrieve the configured proxy request matchers.
     */
    final List<ProxyRequestMatcher> matchers = []

    @Override
    ProxyExpectations any(String path) {
        matches Matchers.any(HttpMethod), path
    }

    @Override
    ProxyExpectations any(Matcher<String> matcher) {
        matches Matchers.any(HttpMethod), matcher
    }

    @Override
    ProxyExpectations get(String path) {
        matches Matchers.equalTo(HttpMethod.GET), path
    }

    @Override
    ProxyExpectations get(Matcher<String> matcher) {
        matches Matchers.equalTo(HttpMethod.GET), matcher
    }

    @Override
    ProxyExpectations head(String path) {
        matches Matchers.equalTo(HttpMethod.HEAD), path
    }

    @Override
    ProxyExpectations head(Matcher<String> matcher) {
        matches Matchers.equalTo(HttpMethod.HEAD), matcher
    }

    @Override
    ProxyExpectations put(String path) {
        matches Matchers.equalTo(HttpMethod.PUT), path
    }

    @Override
    ProxyExpectations put(Matcher<String> matcher) {
        matches Matchers.equalTo(HttpMethod.PUT), matcher
    }

    @Override
    ProxyExpectations post(String path) {
        matches Matchers.equalTo(HttpMethod.POST), path
    }

    @Override
    ProxyExpectations post(Matcher<String> matcher) {
        matches Matchers.equalTo(HttpMethod.POST), matcher
    }

    @Override
    ProxyExpectations delete(String path) {
        matches Matchers.equalTo(HttpMethod.DELETE), path
    }

    @Override
    ProxyExpectations delete(Matcher<String> matcher) {
        matches Matchers.equalTo(HttpMethod.DELETE), matcher
    }

    @Override
    ProxyExpectations patch(String path) {
        matches Matchers.equalTo(HttpMethod.PATCH), path
    }

    @Override
    ProxyExpectations patch(Matcher<String> matcher) {
        matches Matchers.equalTo(HttpMethod.PATCH), matcher
    }

    @Override
    ProxyExpectations options(String path) {
        matches Matchers.equalTo(HttpMethod.OPTIONS), path
    }

    @Override
    ProxyExpectations options(Matcher<String> matcher) {
        matches Matchers.equalTo(HttpMethod.OPTIONS), matcher
    }

    private ProxyExpectations matches(final Matcher<HttpMethod> method, final String path) {
        matches method, Matchers.equalTo(path)
    }

    private ProxyExpectations matches(final Matcher<HttpMethod> method, final Matcher<String> matcher) {
        matchers << new ProxyRequestMatcher(method, matcher)
        this
    }
}