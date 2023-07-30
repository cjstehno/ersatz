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
 * The <code>Expectations</code> interface is the root element of the expectation configuration, which provides the
 * ability to define request expectations and responses for test interactions.
 * <p>
 * Internal expectation matching is done using <a href="http://hamcrest.org/" target="_blank">Hamcrest</a>
 * <code>Matcher</code>s - the methods without explicit Matches provide one as a convenience based on the property and
 * value type (see method description). All configured matchers must match for a specific expectation to be considered
 * a match and if there are multiple matching expectations, the first one configured will be the one considered as the
 * match.
 */
public interface Expectations extends AnyExpectations, GetExpectations, HeadExpectations, PostExpectations, PutExpectations, DeleteExpectations, PatchExpectations, OptionsExpectations, WSExpectations {

    /**
     * Allows configuration of a request expectation matching any request method.
     *
     * @param method the request method
     * @param path   the expected request path
     * @return a <code>Request</code> configuration object
     */
    default Request request(final HttpMethod method, final String path) {
        return request(method, pathMatching(path));
    }

    /**
     * Allows configuration of a request expectation matching any request method.
     *
     * @param method  the request method
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    default Request request(final HttpMethod method, final Matcher<String> matcher) {
        return request(method, pathMatching(matcher));
    }

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param method   the request method
     * @param path     the expected request path
     * @param consumer the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request request(final HttpMethod method, final String path, Consumer<Request> consumer) {
        return request(method, pathMatching(path), consumer);
    }

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer&lt;Request&gt;</code>. The
     * <code>Consumer&lt;Request&gt;</code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param method   the request method
     * @param matcher  the path matcher
     * @param consumer the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request request(final HttpMethod method, final Matcher<String> matcher, final Consumer<Request> consumer) {
        return request(method, pathMatching(matcher), consumer);
    }

    /**
     * Allows configuration of request expectation matching any request method with a path matching the provided
     * matcher.
     *
     * @param method      the request method
     * @param pathMatcher the patch matcher
     * @return a <code>Request</code> configuration object
     */
    default Request request(final HttpMethod method, final PathMatcher pathMatcher) {
        return ANY(pathMatcher, null);
    }

    /**
     * Allows configuration of request expectation matching any request method with a path matching the provided
     * matcher. The consumer will be used to provide addition expectations on the request.
     *
     * @param method      the request method
     * @param pathMatcher the patch matcher
     * @param consumer    the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request request(final HttpMethod method, final PathMatcher pathMatcher, Consumer<Request> consumer);
}
