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
 * Defines the available DELETE request expectations.
 */
public interface DeleteExpectations {

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    default Request DELETE(String path) {
        return DELETE(pathMatcher(path));
    }

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    default Request DELETE(Matcher<String> matcher) {
        return DELETE(matcher, (Consumer<Request>) null);
    }

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The <code>Consumer&lt;Request&gt;</code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    default Request DELETE(String path, Consumer<Request> config) {
        return DELETE(pathMatcher(path), config);
    }

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The <code>Consumer&lt;Request&gt;</code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request DELETE(Matcher<String> matcher, Consumer<Request> config);
}
