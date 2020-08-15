/**
 * Copyright (C) 2020 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.ProxyConfig;
import com.stehno.ersatz.cfg.ProxyExpectations;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Implementation of the ProxyConfig interface providing the configuration functionality for the proxy server.
 */
public class ProxyConfigImpl implements ProxyConfig {

    private static final String ONLY_HTTP_MESSAGE = "Only HTTP targets are supported at this time.";
    private static final String HTTP = "http";

    private final ProxyExpectationsImpl expectations = new ProxyExpectationsImpl();
    private boolean autoStart = true;
    private URI targetUri;

    @Override
    public ProxyConfig autoStart(boolean auto) {
        autoStart = auto;
        return this;
    }

    @Override
    public ProxyConfig target(final URI value) {
        ensure(value.getScheme().equalsIgnoreCase(HTTP), ONLY_HTTP_MESSAGE);
        targetUri = value;
        return this;
    }

    @Override
    public ProxyConfig expectations(Consumer<ProxyExpectations> consumer) {
        consumer.accept(expectations);
        return this;
    }

    public ProxyExpectationsImpl getExpectations() {
        return expectations;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    private static void ensure(final boolean isTrue, final String message) {
        if (!isTrue) {
            throw new IllegalArgumentException(message);
        }
    }
}