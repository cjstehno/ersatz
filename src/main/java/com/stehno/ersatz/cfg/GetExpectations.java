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

public interface GetExpectations {

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    default Request GET(String path) {
        return GET(pathMatcher(path));
    }

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param matcher the path matcher.
     * @return a <code>Request</code> configuration object
     */
    default Request GET(Matcher<String> matcher) {
        return GET(matcher, (Consumer<Request>) null);
    }

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    default Request GET(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return GET(pathMatcher(path), closure);
    }

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    default Request GET(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return GET(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    default Request GET(String path, Consumer<Request> config) {
        return GET(pathMatcher(path), config);
    }

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request GET(Matcher<String> matcher, Consumer<Request> config);
}
