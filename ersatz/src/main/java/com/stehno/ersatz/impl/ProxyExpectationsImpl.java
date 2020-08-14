/**
 * Copyright (C) 2020 Christopher J. Stehno
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
import com.stehno.ersatz.cfg.ProxyExpectations;
import org.hamcrest.Matcher;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;

/**
 * Implementation of the ProxyExpectations interface providing configuration of the expected requests to the proxy server.
 */
public class ProxyExpectationsImpl implements ProxyExpectations {

    private final List<ProxyRequestMatcher> matchers = new LinkedList<>();

    public List<ProxyRequestMatcher> getMatchers() {
        return matchers;
    }

    @Override
    public ProxyExpectations ANY(String path) {
        return matches(any(HttpMethod.class), path);
    }

    @Override
    public ProxyExpectations ANY(Matcher<String> matcher) {
        return matches(any(HttpMethod.class), matcher);
    }

    @Override
    public ProxyExpectations GET(String path) {
        return matches(equalTo(HttpMethod.GET), path);
    }

    @Override
    public ProxyExpectations GET(Matcher<String> matcher) {
        return matches(equalTo(HttpMethod.GET), matcher);
    }

    @Override
    public ProxyExpectations HEAD(String path) {
        return matches(equalTo(HttpMethod.HEAD), path);
    }

    @Override
    public ProxyExpectations HEAD(Matcher<String> matcher) {
        return matches(equalTo(HttpMethod.HEAD), matcher);
    }

    @Override
    public ProxyExpectations PUT(String path) {
        return matches(equalTo(HttpMethod.PUT), path);
    }

    @Override
    public ProxyExpectations PUT(Matcher<String> matcher) {
        return matches(equalTo(HttpMethod.PUT), matcher);
    }

    @Override
    public ProxyExpectations POST(String path) {
        return matches(equalTo(HttpMethod.POST), path);
    }

    @Override
    public ProxyExpectations POST(Matcher<String> matcher) {
        return matches(equalTo(HttpMethod.POST), matcher);
    }

    @Override
    public ProxyExpectations DELETE(String path) {
        return matches(equalTo(HttpMethod.DELETE), path);
    }

    @Override
    public ProxyExpectations DELETE(Matcher<String> matcher) {
        return matches(equalTo(HttpMethod.DELETE), matcher);
    }

    @Override
    public ProxyExpectations PATCH(String path) {
        return matches(equalTo(HttpMethod.PATCH), path);
    }

    @Override
    public ProxyExpectations PATCH(Matcher<String> matcher) {
        return matches(equalTo(HttpMethod.PATCH), matcher);
    }

    @Override
    public ProxyExpectations OPTIONS(String path) {
        return matches(equalTo(HttpMethod.OPTIONS), path);
    }

    @Override
    public ProxyExpectations OPTIONS(Matcher<String> matcher) {
        return matches(equalTo(HttpMethod.OPTIONS), matcher);
    }

    private ProxyExpectations matches(final Matcher<HttpMethod> method, final String path) {
        return matches(method, equalTo(path));
    }

    private ProxyExpectations matches(final Matcher<HttpMethod> method, final Matcher<String> matcher) {
        matchers.add(new ProxyRequestMatcher(method, matcher));
        return this;
    }
}