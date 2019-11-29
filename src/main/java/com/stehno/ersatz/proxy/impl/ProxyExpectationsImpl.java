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
package com.stehno.ersatz.proxy.impl;

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.proxy.ProxyExpectations;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.LinkedList;
import java.util.List;

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
        return matches(Matchers.any(HttpMethod.class), path);
    }

    @Override
    public ProxyExpectations ANY(Matcher<String> matcher) {
        return matches(Matchers.any(HttpMethod.class), matcher);
    }

    @Override
    public ProxyExpectations GET(String path) {
        return matches(Matchers.equalTo(HttpMethod.GET), path);
    }

    @Override
    public ProxyExpectations GET(Matcher<String> matcher) {
        return matches( Matchers.equalTo(HttpMethod.GET), matcher);
    }

    @Override
    public ProxyExpectations HEAD(String path) {
        return matches( Matchers.equalTo(HttpMethod.HEAD), path);
    }

    @Override
    public ProxyExpectations HEAD(Matcher<String> matcher) {
        return matches( Matchers.equalTo(HttpMethod.HEAD), matcher);
    }

    @Override
    public ProxyExpectations PUT(String path) {
        return matches( Matchers.equalTo(HttpMethod.PUT), path);
    }

    @Override
    public ProxyExpectations PUT(Matcher<String> matcher) {
        return matches( Matchers.equalTo(HttpMethod.PUT), matcher);
    }

    @Override
    public ProxyExpectations POST(String path) {
        return matches( Matchers.equalTo(HttpMethod.POST), path);
    }

    @Override
    public ProxyExpectations POST(Matcher<String> matcher) {
        return matches( Matchers.equalTo(HttpMethod.POST), matcher);
    }

    @Override
    public ProxyExpectations DELETE(String path) {
        return matches( Matchers.equalTo(HttpMethod.DELETE), path);
    }

    @Override
    public ProxyExpectations DELETE(Matcher<String> matcher) {
        return matches( Matchers.equalTo(HttpMethod.DELETE), matcher);
    }

    @Override
    public ProxyExpectations PATCH(String path) {
        return matches( Matchers.equalTo(HttpMethod.PATCH), path);
    }

    @Override
    public ProxyExpectations PATCH(Matcher<String> matcher) {
        return matches( Matchers.equalTo(HttpMethod.PATCH), matcher);
    }

    @Override
    public ProxyExpectations OPTIONS(String path) {
        return matches( Matchers.equalTo(HttpMethod.OPTIONS), path);
    }

    @Override
    public ProxyExpectations OPTIONS(Matcher<String> matcher) {
        return matches( Matchers.equalTo(HttpMethod.OPTIONS), matcher);
    }

    private ProxyExpectations matches(final Matcher<HttpMethod> method, final String path) {
        return matches( method, Matchers.equalTo(path));
    }

    private ProxyExpectations matches(final Matcher<HttpMethod> method, final Matcher<String> matcher) {
        matchers.add(new ProxyRequestMatcher(method, matcher));
        return this;
    }
}