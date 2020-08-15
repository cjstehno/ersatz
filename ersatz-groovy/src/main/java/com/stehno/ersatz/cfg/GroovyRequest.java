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

import static groovy.lang.Closure.DELEGATE_FIRST;

public interface GroovyRequest extends Request {

    /**
     * Allows for configuration of a <code>Response</code> by the given Groovy <code>Closure</code>, which will delegate to a <code>Response</code>
     * instance passed into it for configuration using the Groovy DSL.
     *
     * @param closure the <code>Consumer&lt;Response&gt;</code> to provide configuration of the response
     * @return a reference to this request
     */
    GroovyRequest responder(@DelegatesTo(value = GroovyResponse.class, strategy = DELEGATE_FIRST) final Closure closure);
}
