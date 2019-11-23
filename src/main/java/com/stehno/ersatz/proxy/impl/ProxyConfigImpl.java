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

import com.stehno.ersatz.proxy.ProxyConfig;
import com.stehno.ersatz.proxy.ProxyExpectations;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Implementation of the ProxyConfig interface providing the configuration functionality for the proxy server.
 */
public class ProxyConfigImpl implements ProxyConfig {

    private static final String ONLY_HTTP_MESSAGE = "Only HTTP targets are supported at this time.";
    private static final String HTTP = "http";

    private boolean autoStart = true;
    private URI targetUri;
    private final ProxyExpectationsImpl expectations = new ProxyExpectationsImpl();

    public boolean isAutoStart() {
        return autoStart;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public ProxyExpectationsImpl getExpectations() {
        return expectations;
    }

    @Override
    public ProxyConfig autoStart(boolean auto) {
        autoStart = auto;
        return this;
    }

    @Override
    public ProxyConfig target(String value) {
        ensure(value.toLowerCase().startsWith("http://"), ONLY_HTTP_MESSAGE);
        try {
            targetUri = new URI(value);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ProxyConfig target(URI value) {
        ensure(value.getScheme().equalsIgnoreCase(HTTP), ONLY_HTTP_MESSAGE);
        targetUri = value;
        return this;
    }

    @Override
    public ProxyConfig target(URL value) {
        ensure(value.getProtocol().equalsIgnoreCase(HTTP), ONLY_HTTP_MESSAGE);
        try {
            targetUri = value.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ProxyConfig expectations(@DelegatesTo(value = ProxyExpectations.class, strategy = DELEGATE_FIRST) Closure closure) {
        return expectations(ConsumerWithDelegate.create(closure));
    }

    @Override
    public ProxyConfig expectations(Consumer<ProxyExpectations> consumer) {
        consumer.accept(expectations);
        return this;
    }

    private static void ensure(final boolean isTrue, final String message) {
        if (!isTrue) {
            throw new IllegalArgumentException(message);
        }
    }
}