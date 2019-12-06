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

import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * The <code>Expectations</code> interface is the root element of the expectation configuration, which provides the
 * ability to define request expectations and responses for test interactions.
 * <p>
 * Internal expectation matching is done using <a href="http://hamcrest.org/" target="_blank">Hamcrest</a>
 * <code>Matcher</code>s - the methods without explicit Matches provide one as a convenience based on the property and
 * value type (see method description). All configured matchers must match for a specific expectation to be considered
 * a match and if there are multiple matching expectations, the first one configured will be the one considered as the
 * match.
 */
public interface Expectations extends AnyExpectations, GetExpectations, HeadExpectations, PostExpectations, PutExpectations, DeleteExpectations, PatchExpectations, OptionsExpectations {

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection
     * will be expected in order for the verification to pass.
     */
    default WebSocketExpectations ws(String path){
        return ws(path, (Consumer<WebSocketExpectations>) null);
    }

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection
     * will be expected in order for the verification to pass.
     */
    default WebSocketExpectations ws(String path, @DelegatesTo(value = WebSocketExpectations.class, strategy = DELEGATE_FIRST) Closure closure){
        return ws(path, ConsumerWithDelegate.create(closure));
    }

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection
     * will be expected in order for the verification to pass.
     */
    WebSocketExpectations ws(String path, Consumer<WebSocketExpectations> config);
}
