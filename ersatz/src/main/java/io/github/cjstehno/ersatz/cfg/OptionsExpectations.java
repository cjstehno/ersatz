/**
 * Copyright (C) 2024 Christopher J. Stehno
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
 * Defines the available OPTIONS request expectations.
 */
@SuppressWarnings("checkstyle:MethodName")
public interface OptionsExpectations {

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    default Request OPTIONS(final String path) {
        return OPTIONS(pathMatching(path));
    }

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    default Request OPTIONS(final Matcher<String> matcher) {
        return OPTIONS(pathMatching(matcher));
    }

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer&lt;Request&gt;</code>.
     * The <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path   the expected request path
     * @param config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request OPTIONS(final String path, final Consumer<Request> config) {
        return OPTIONS(pathMatching(path), config);
    }

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param config  the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request OPTIONS(final Matcher<String> matcher, final Consumer<Request> config) {
        return OPTIONS(pathMatching(matcher), config);
    }

    /**
     * Allows configuration of an OPTIONS request expectation.
     *
     * @param pathMatcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    default Request OPTIONS(final PathMatcher pathMatcher) {
        return OPTIONS(pathMatcher, null);
    }

    /**
     * Allows configuration of an OPTIONS request expectation using the provided <code>Consumer</code>.
     *
     * @param pathMatcher the path matcher
     * @param config  the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request OPTIONS(PathMatcher pathMatcher, Consumer<Request> config);
}
