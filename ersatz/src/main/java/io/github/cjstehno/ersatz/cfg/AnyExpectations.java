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

import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;

import io.github.cjstehno.ersatz.match.PathMatcher;
import java.util.function.Consumer;
import org.hamcrest.Matcher;

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
    default Request ANY(final String path) {
        return ANY(pathMatching(path));
    }

    /**
     * Allows configuration of a request expectation matching any request method.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    default Request ANY(final Matcher<String> matcher) {
        return ANY(pathMatching(matcher));
    }

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @param consumer the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request ANY(final String path, Consumer<Request> consumer) {
        return ANY(pathMatching(path), consumer);
    }

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param consumer the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request ANY(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return ANY(pathMatching(matcher), consumer);
    }

    /**
     * Allows configuration of request expectation matching any request method with a path matching the provided
     * matcher.
     *
     * @param pathMatcher the patch matcher
     * @return a <code>Request</code> configuration object
     */
    default Request ANY(final PathMatcher pathMatcher) {
        return ANY(pathMatcher, null);
    }

    /**
     * Allows configuration of request expectation matching any request method with a path matching the provided
     * matcher. The consumer will be used to provide addition expectations on the request.
     *
     * @param pathMatcher the patch matcher
     * @param consumer the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request ANY(final PathMatcher pathMatcher, Consumer<Request> consumer);
}
