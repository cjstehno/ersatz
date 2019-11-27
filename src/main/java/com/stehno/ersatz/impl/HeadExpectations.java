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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.ErsatzMatchers;
import com.stehno.ersatz.Request;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.util.function.Consumer;

import static com.stehno.ersatz.ErsatzMatchers.pathMatcher;
import static groovy.lang.Closure.DELEGATE_FIRST;
import static org.hamcrest.Matchers.equalTo;

public interface HeadExpectations {

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true)
    default Request head(String path) {
        return HEAD(path);
    }

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true)
    default Request head(Matcher<String> matcher) {
        return HEAD(matcher);
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true)
    default Request head(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return HEAD(path, closure);
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true)
    default Request head(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return HEAD(matcher, closure);
    }

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path   the expected request path
     * @param config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true)
    default Request head(String path, Consumer<Request> config) {
        return HEAD(path, config);
    }

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param config  the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true)
    default Request head(Matcher<String> matcher, Consumer<Request> config) {
        return HEAD(matcher, config);
    }

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
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
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
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param config  the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request HEAD(Matcher<String> matcher, Consumer<Request> config);
}
