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

import com.stehno.ersatz.proxy.ProxyConfig
import com.stehno.ersatz.proxy.ProxyExpectations
import com.stehno.vanilla.Affirmations
import groovy.transform.CompileStatic

import java.util.function.Consumer

/**
 * Implementation of the ProxyConfig interface providing the configuration functionality for the proxy server.
 */
@CompileStatic
class ProxyConfigImpl implements ProxyConfig {

    /**
     * The configured auto-start value.
     */
    boolean autoStart = true

    /**
     * The configured target URI value.
     */
    URI targetUri

    /**
     * The configured expectations.
     */
    final ProxyExpectationsImpl expectations = new ProxyExpectationsImpl()

    private static final String ONLY_HTTP_MESSAGE = 'Only HTTP targets are supported at this time.'
    private static final String HTTP = 'http'

    @Override
    ProxyConfig autoStart(boolean auto) {
        autoStart = auto
        this
    }

    @Override
    ProxyConfig target(String value) {
        Affirmations.affirm value.toLowerCase().startsWith('http://'), ONLY_HTTP_MESSAGE
        targetUri = value.toURI()
        this
    }

    @Override
    ProxyConfig target(URI value) {
        Affirmations.affirm value.scheme.equalsIgnoreCase(HTTP), ONLY_HTTP_MESSAGE
        targetUri = value
        this
    }

    @Override
    ProxyConfig target(URL value) {
        Affirmations.affirm value.protocol.equalsIgnoreCase(HTTP), ONLY_HTTP_MESSAGE
        targetUri = value.toURI()
        this
    }

    @Override
    ProxyConfig expectations(@DelegatesTo(ProxyExpectations) Closure closure) {
        closure.delegate = expectations
        closure.call()
        this
    }

    @Override
    ProxyConfig expectations(Consumer<ProxyExpectations> consumer) {
        consumer.accept(expectations)
        this
    }
}