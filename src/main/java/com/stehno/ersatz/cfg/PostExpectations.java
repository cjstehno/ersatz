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
 * Defines the available POST request expectations.
 */
public interface PostExpectations {

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param path the request path.
     * @return a <code>RequestWithContent</code> configuration object
     */
    default RequestWithContent POST(String path) {
        return POST(pathMatcher(path));
    }

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    default RequestWithContent POST(Matcher<String> matcher) {
        return POST(matcher, (Consumer<RequestWithContent>) null);
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    default RequestWithContent POST(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return POST(pathMatcher(path), closure);
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    default RequestWithContent POST(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return POST(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer&lt;RequestWithContent&gt;</code>. The
     * <code>Consumer&lt;RequestWithContent&gt;</code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    default RequestWithContent POST(String path, Consumer<RequestWithContent> config) {
        return POST(pathMatcher(path), config);
    }

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer&lt;RequestWithContent&gt;</code>. The
     * <code>Consumer&lt;RequestWithContent&gt;</code>will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent POST(Matcher<String> matcher, Consumer<RequestWithContent> config);
}
