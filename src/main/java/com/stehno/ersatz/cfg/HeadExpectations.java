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
import org.hamcrest.Matcher;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.util.function.Consumer;

import static com.stehno.ersatz.match.ErsatzMatchers.pathMatcher;
import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Defines the available HEAD request expectations.
 */
public interface HeadExpectations {

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    default Request HEAD(String path) {
        return HEAD(pathMatcher(path));
    }

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    default Request HEAD(Matcher<String> matcher) {
        return HEAD(matcher, (Consumer<Request>) null);
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    default Request HEAD(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return HEAD(pathMatcher(path), closure);
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    default Request HEAD(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return HEAD(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The <code>Consumer&lt;Request&gt;</code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path   the expected request path
     * @param config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request HEAD(String path, Consumer<Request> config) {
        return HEAD(pathMatcher(path), config);
    }

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The <code>Consumer&lt;Request&gt;</code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param config  the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request HEAD(Matcher<String> matcher, Consumer<Request> config);
}
