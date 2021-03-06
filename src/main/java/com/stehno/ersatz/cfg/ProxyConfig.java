/**
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
package com.stehno.ersatz.cfg;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Configuration DSL interface for the ErsatzProxy server.
 */
public interface ProxyConfig {

    /**
     * Toggles the server auto-start feature. By default the proxy server will start once it is configured.
     *
     * @param auto enable/disable auto-start
     * @return a reference to this configuration
     */
    ProxyConfig autoStart(boolean auto);

    /**
     * Specifies the target URI for the proxy server.
     *
     * @param value the target URI
     * @return a reference to this configuration
     */
    default ProxyConfig target(String value) {
        try {
            return target(new URI(value));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Specifies the target URI for the proxy server.
     *
     * @param value the target URI
     * @return a reference to this configuration
     */
    ProxyConfig target(URI value);

    /**
     * Specifies the target URI for the proxy server.
     *
     * @param value the target URI
     * @return a reference to this configuration
     */
    default ProxyConfig target(URL value) {
        try {
            return target(value.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Used to configure the proxy server expectations with a Groovy Closure, which delegates to an instance of ProxyExpectations.
     *
     * @param closure the Groovy closure
     * @return a reference to this configuration
     */
    default ProxyConfig expectations(@DelegatesTo(value = ProxyExpectations.class, strategy = DELEGATE_FIRST) Closure closure) {
        return expectations(ConsumerWithDelegate.create(closure));
    }

    /**
     * Used to configure the proxy server expectations with a Consumer, which will have an instance of ProxyExpectations passed into it.
     *
     * @param consumer the configuration consumer
     * @return a reference to this configuration
     */
    ProxyConfig expectations(Consumer<ProxyExpectations> consumer);
}
