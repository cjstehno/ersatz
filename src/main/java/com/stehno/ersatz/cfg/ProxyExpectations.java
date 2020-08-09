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

/**
 * Proxy server expectation configuration interface. Provides a means of configuring the requests that are expected to be proxied by the server.
 */
 public interface ProxyExpectations {

    /**
     * Configures an expected request with any request method and the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations ANY(String path);

    /**
     * Configures an expected request with any request method using the given path matcher.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations ANY(Matcher<String> matcher);

    /**
     * Configures an expected GET request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations GET(String path);

    /**
     * Configures an expected GET request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations GET(Matcher<String> matcher);

    /**
     * Configures an expected HEAD request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations HEAD(String path);

    /**
     * Configures an expected HEAD request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations HEAD(Matcher<String> matcher);

    /**
     * Configures an expected PUT request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations PUT(String path);

    /**
     * Configures an expected PUT request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations PUT(Matcher<String> matcher);

    /**
     * Configures an expected POST request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations POST(String path);

    /**
     * Configures an expected POST request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations POST(Matcher<String> matcher);

    /**
     * Configures an expected DELETE request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations DELETE(String path);

    /**
     * Configures an expected DELETE request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations DELETE(Matcher<String> matcher);

    /**
     * Configures an expected PATCH request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations PATCH(String path);

    /**
     * Configures an expected PATCH request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations PATCH(Matcher<String> matcher);

    /**
     * Configures an expected OPTIONS request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations OPTIONS(String path);

    /**
     * Configures an expected OPTIONS request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations OPTIONS(Matcher<String> matcher);
}
