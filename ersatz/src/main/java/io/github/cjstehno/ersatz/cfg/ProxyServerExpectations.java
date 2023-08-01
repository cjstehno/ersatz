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

/**
 * Defines the expectation configurations available for the proxy server.
 */
public interface ProxyServerExpectations {

    /**
     * Configures an expected request with any request method and the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations any(final String path) {
        return any(pathMatching(path));
    }

    /**
     * Configures an expected request with any request method using the given path matcher.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations any(final PathMatcher matcher);

    /**
     * Configures an expected GET request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations get(final String path) {
        return get(pathMatching(path));
    }

    /**
     * Configures an expected GET request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations get(final PathMatcher matcher);

    /**
     * Configures an expected HEAD request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations head(final String path) {
        return head(pathMatching(path));
    }

    /**
     * Configures an expected HEAD request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations head(final PathMatcher matcher);

    /**
     * Configures an expected PUT request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations put(final String path) {
        return put(pathMatching(path));
    }

    /**
     * Configures an expected PUT request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations put(final PathMatcher matcher);

    /**
     * Configures an expected POST request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations post(final String path) {
        return post(pathMatching(path));
    }

    /**
     * Configures an expected POST request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations post(final PathMatcher matcher);

    /**
     * Configures an expected DELETE request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations delete(final String path) {
        return delete(pathMatching(path));
    }

    /**
     * Configures an expected DELETE request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations delete(final PathMatcher matcher);

    /**
     * Configures an expected PATCH request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations patch(final String path) {
        return patch(pathMatching(path));
    }

    /**
     * Configures an expected PATCH request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations patch(final PathMatcher matcher);

    /**
     * Configures an expected OPTIONS request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    default ProxyServerExpectations options(final String path) {
        return options(pathMatching(path));
    }

    /**
     * Configures an expected OPTIONS request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyServerExpectations options(final PathMatcher matcher);
}
