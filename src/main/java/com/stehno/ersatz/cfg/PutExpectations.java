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
 * Defines the available PUT request expectations.
 */
public interface PutExpectations {

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     */
    default RequestWithContent PUT(String path) {
        return PUT(pathMatcher(path));
    }

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    default RequestWithContent PUT(Matcher<String> matcher) {
        return PUT(matcher, (Consumer<RequestWithContent>) null);
    }

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    default RequestWithContent PUT(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PUT(pathMatcher(path), closure);
    }

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    default RequestWithContent PUT(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PUT(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer&lt;RequestWithContent&gt;</code>. The
     * <code>Consumer&lt;RequestWithContent&gt;</code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    default RequestWithContent PUT(String path, Consumer<RequestWithContent> config) {
        return PUT(pathMatcher(path), config);
    }

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer&lt;RequestWithContent&gt;</code>. The
     * <code>Consumer&lt;RequestWithContent&gt;</code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent PUT(Matcher<String> matcher, Consumer<RequestWithContent> config);
}
