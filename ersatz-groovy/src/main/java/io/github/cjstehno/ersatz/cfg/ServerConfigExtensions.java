/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.cfg;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Groovy extensions to the Ersatz server configuration providing Groovy DSL behavior.
 */
public class ServerConfigExtensions {

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     * <p>
     * If auto-start is enabled (default) the server will be started after the expectations are applied.
     *
     * @param self    the type of object being extended
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server config
     */
    public static ServerConfig expectations(
        final ServerConfig self,
        @DelegatesTo(value = Expectations.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.expectations(ConsumerWithDelegate.create(closure));
    }

    /**
     * Used to configure any global request requirements using the provided Groovy Closure.
     *
     * @param self    the type of object being extended.
     * @param closure the Groovy Closure which will provide the configuration
     * @return a reference to the server config
     */
    public static ServerConfig requirements(
        final ServerConfig self,
        @DelegatesTo(value = Requirements.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.requirements(ConsumerWithDelegate.create(closure));
    }
}
