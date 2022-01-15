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

import org.hamcrest.Matcher;

import java.util.function.Consumer;

import static com.stehno.ersatz.match.ErsatzMatchers.pathMatcher;

/**
 * Defines the available generic request (ANY) expectations.
 */
public interface AnyExpectations {

    /**
     * Allows configuration of a request expectation matching any request method.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    default Request ANY(String path) {
        return ANY(pathMatcher(path));
    }

    /**
     * Allows configuration of a request expectation matching any request method.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    default Request ANY(Matcher<String> matcher) {
        return ANY(matcher, (Consumer<Request>) null);
    }

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @param consumer the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request ANY(String path, Consumer<Request> consumer) {
        return ANY(pathMatcher(path), consumer);
    }

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param consumer the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request ANY(Matcher<String> matcher, Consumer<Request> consumer);
}
