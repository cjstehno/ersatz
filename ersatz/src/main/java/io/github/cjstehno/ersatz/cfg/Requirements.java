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

import static io.github.cjstehno.ersatz.match.HttpMethodMatcher.methodMatching;

import io.github.cjstehno.ersatz.match.HttpMethodMatcher;
import io.github.cjstehno.ersatz.match.PathMatcher;
import java.util.function.Consumer;

/**
 * Defines the available requirements configuration methods.
 * <p>
 * The requirements to be met by a request are determined by matching the request method and path.
 * If no requirements match the request method and path, the request will be allowed to continue.
 * A request that matches the configured method and path must meet the configured requirements, or it will be
 * considered invalid.
 */
public interface Requirements {

    /**
     * Allows the configuration of a global request requirement with the provided parameters.
     *
     * @param method      the method
     * @param pathMatcher the path matcher
     * @param config      the requirements configuration
     * @return a reference to the request requirement for chained configuration
     */
    default RequestRequirement that(final HttpMethod method, final PathMatcher pathMatcher, final Consumer<RequestRequirement> config) {
        return that(methodMatching(method), pathMatcher, config);
    }

    /**
     * Allows the configuration of a global request requirement with the provided parameters.
     *
     * @param method      the method
     * @param pathMatcher the path matcher
     * @return a reference to the request requirement for chained configuration
     */
    default RequestRequirement that(final HttpMethod method, final PathMatcher pathMatcher) {
        return that(methodMatching(method), pathMatcher);
    }

    /**
     * Allows the configuration of a global request requirement with the provided parameters.
     *
     * @param methodMatcher the method mather
     * @param pathMatcher   the path matcher
     * @return a reference to the request requirement for chained configuration
     */
    default RequestRequirement that(final HttpMethodMatcher methodMatcher, final PathMatcher pathMatcher) {
        return that(methodMatcher, pathMatcher, null);
    }

    /**
     * Allows the configuration of a global request requirement with the provided parameters.
     *
     * @param methodMatcher the method matcher
     * @param pathMatcher   the path matcher
     * @param config        the requirements configuration
     * @return a reference to the request requirement for chained configuration
     */
    RequestRequirement that(final HttpMethodMatcher methodMatcher, final PathMatcher pathMatcher, final Consumer<RequestRequirement> config);
}
