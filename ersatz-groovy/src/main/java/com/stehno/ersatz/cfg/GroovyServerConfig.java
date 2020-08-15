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
package com.stehno.ersatz.cfg;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

public interface GroovyServerConfig extends ServerConfig {

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     * <p>
     * If auto-start is enabled (default) the server will be started after the expectations are applied.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    default GroovyServerConfig expectations(@DelegatesTo(value = GroovyExpectations.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return (GroovyServerConfig) expectations(ConsumerWithDelegate.create(closure));
    }

    /**
     * Registers authentication configuration as a Groovy Closure.
     *
     * @param closure the configuration closure
     * @return a reference to this server configuration
     */
    GroovyServerConfig authentication(@DelegatesTo(value = AuthenticationConfig.class, strategy = DELEGATE_FIRST) Closure closure);
}
