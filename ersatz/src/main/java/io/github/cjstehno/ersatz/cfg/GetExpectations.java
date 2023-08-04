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

import io.github.cjstehno.ersatz.match.PathMatcher;
import org.hamcrest.Matcher;

import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;

/**
 * Defines the available GET request expectations.
 */
@SuppressWarnings("checkstyle:MethodName")
public interface GetExpectations {

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    default Request GET(String path) {
        return GET(pathMatching(path));
    }

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param matcher the path matcher.
     * @return a <code>Request</code> configuration object
     */
    default Request GET(final Matcher<String> matcher) {
        return GET(pathMatching(matcher));
    }

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path   the expected request path
     * @param config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request GET(String path, Consumer<Request> config) {
        return GET(pathMatching(path), config);
    }

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param config  the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request GET(final Matcher<String> matcher, final Consumer<Request> config) {
        return GET(pathMatching(matcher), config);
    }

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param pathMatcher the patch matcher
     * @return a <code>Request</code> configuration object
     */
    default Request GET(final PathMatcher pathMatcher) {
        return GET(pathMatcher, null);
    }

    /**
     * Allows configuration of a GET request expectation with the provided <code>Consumer</code>.
     *
     * @param pathMatcher the patch matcher
     * @param config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request GET( PathMatcher pathMatcher,  Consumer<Request> config);
}
