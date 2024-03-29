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
package io.github.cjstehno.ersatz;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.github.cjstehno.ersatz.cfg.Expectations;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.impl.ServerConfigImpl;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * A Groovy extension to the core ErsatzServer library, providing a more Groovy-friendly DSL interface.
 */
public class GroovyErsatzServer extends ErsatzServer {

    /**
     * Creates a new Groovy Ersatz server with no specific configuration.
     */
    public GroovyErsatzServer() {
        super();
    }

    /**
     * Creates a new Groovy Ersatz server with the given server configuration.
     *
     * @param serverConfig the server configuration object
     */
    public GroovyErsatzServer(final ServerConfigImpl serverConfig) {
        super(serverConfig);
    }

    /**
     * Creates a new Groovy Ersatz Server as a wrapper around the provided server instance.
     *
     * @param ersatzServer the server instance to be wrapped
     */
    public GroovyErsatzServer(final ErsatzServer ersatzServer){
        super(ersatzServer.getServerConfig());
    }

    /**
     * Creates a new Ersatz server instance with either the default configuration or a configuration provided by the Groovy DSL closure.
     *
     * @param closure the configuration closure (delegated to <code>ServerConfig</code>)
     */
    public GroovyErsatzServer(@DelegatesTo(value = ServerConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        super(ConsumerWithDelegate.create(closure));
    }

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     * <p>
     * Calling this method when auto-start is enabled will start the server.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    public GroovyErsatzServer expectations(@DelegatesTo(value = Expectations.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return (GroovyErsatzServer) expectations(ConsumerWithDelegate.create(closure));
    }
}
