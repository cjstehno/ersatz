/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.proxy

import groovy.transform.CompileStatic
import org.hamcrest.Matcher

/**
 * Proxy server expectation configuration interface. Provides a means of configuring the requests that are expected to be proxied by the server.
 */
@CompileStatic @SuppressWarnings('MethodCount')
interface ProxyExpectations {

    /**
     * Configures an expected request with any request method and the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations any(String path)

    /**
     * Configures an expected request with any request method using the given path matcher.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations any(Matcher<String> matcher)

    /**
     * Configures an expected GET request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations get(String path)

    /**
     * Configures an expected GET request using the provided matcher to match the path.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations get(Matcher<String> matcher)

    /**
     * Configures an expected HEAD request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations head(String path)

    /**
     * Configures an expected HEAD request using the provided matcher to match the path.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations head(Matcher<String> matcher)

    /**
     * Configures an expected PUT request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations put(String path)

    /**
     * Configures an expected PUT request using the provided matcher to match the path.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations put(Matcher<String> matcher)

    /**
     * Configures an expected POST request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations post(String path)

    /**
     * Configures an expected POST request using the provided matcher to match the path.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations post(Matcher<String> matcher)

    /**
     * Configures an expected DELETE request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations delete(String path)

    /**
     * Configures an expected DELETE request using the provided matcher to match the path.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations delete(Matcher<String> matcher)

    /**
     * Configures an expected PATCH request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations patch(String path)

    /**
     * Configures an expected PATCH request using the provided matcher to match the path.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations patch(Matcher<String> matcher)

    /**
     * Configures an expected OPTIONS request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations options(String path)

    /**
     * Configures an expected OPTIONS request using the provided matcher to match the path.
     *
     * @param path the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations options(Matcher<String> matcher)
}