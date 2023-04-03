/**
 * Copyright (C) 2023 Christopher J. Stehno
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
import io.github.cjstehno.ersatz.cfg.ProxyServerConfig;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Groovy extensions for the <code>ErsatzProxyServer</code> class.
 */
public class GroovyErsatzProxyServer extends ErsatzProxyServer {

    /**
     * Creates a new proxy server with the specified configuration. The configuration closure will delegate to an
     * instance of <code>ProxyServerConfig</code> for the actual configuration.
     * <p>
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param closure the configuration closure.
     */
    public GroovyErsatzProxyServer(@DelegatesTo(value = ProxyServerConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        super(ConsumerWithDelegate.create(closure));
    }
}
