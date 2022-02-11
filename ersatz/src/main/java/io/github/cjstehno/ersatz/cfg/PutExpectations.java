/**
 * Copyright (C) 2022 Christopher J. Stehno
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
    default RequestWithContent PUT(final String path) {
        return PUT(PathMatcher.pathMatching(path));
    }

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    default RequestWithContent PUT(final Matcher<String> matcher) {
        return PUT(PathMatcher.pathMatching(matcher));
    }

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer&lt;RequestWithContent&gt;</code>. The
     * <code>Consumer&lt;RequestWithContent&gt;</code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    default RequestWithContent PUT(final String path, final Consumer<RequestWithContent> config) {
        return PUT(PathMatcher.pathMatching(path), config);
    }

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer&lt;RequestWithContent&gt;</code>. The
     * <code>Consumer&lt;RequestWithContent&gt;</code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    default RequestWithContent PUT(final Matcher<String> matcher, final Consumer<RequestWithContent> config) {
        return PUT(PathMatcher.pathMatching(matcher), config);
    }

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param pathMatcher the patch matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    default RequestWithContent PUT(final PathMatcher pathMatcher) {
        return PUT(pathMatcher, null);
    }

    /**
     * Allows configuration of a PUT request expectation with the provided <code>Consumer</code>.
     *
     * @param pathMatcher the patch matcher
     * @param config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent PUT(final PathMatcher pathMatcher, final Consumer<RequestWithContent> config);
}
