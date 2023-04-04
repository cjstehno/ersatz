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
package io.github.cjstehno.ersatz.cfg;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Groovy extensions for the <code>ProxyServerConfig</code> class.
 */
public class ProxyServerConfigExtensions {

    /**
     * Used to configure the proxy server expectations with a Groovy Closure, which delegates to an instance of
     * ProxyExpectations.
     *
     * @param closure the Groovy closure
     * @return a reference to this configuration
     */
    ProxyServerConfig expectations(
        final ProxyServerConfig self,
        @DelegatesTo(value = ProxyServerExpectations.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.expectations(ConsumerWithDelegate.create(closure));
    }

}