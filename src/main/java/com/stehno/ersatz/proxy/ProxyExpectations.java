package com.stehno.ersatz.proxy;

import org.hamcrest.Matcher;

/**
 * Proxy server expectation configuration interface. Provides a means of configuring the requests that are expected to be proxied by the server.
 */
@SuppressWarnings("MethodCount") public interface ProxyExpectations {

    /**
     * Configures an expected request with any request method and the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations any(String path);

    /**
     * Configures an expected request with any request method using the given path matcher.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations any(Matcher<String> matcher);

    /**
     * Configures an expected GET request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations get(String path);

    /**
     * Configures an expected GET request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations get(Matcher<String> matcher);

    /**
     * Configures an expected HEAD request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations head(String path);

    /**
     * Configures an expected HEAD request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations head(Matcher<String> matcher);

    /**
     * Configures an expected PUT request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations put(String path);

    /**
     * Configures an expected PUT request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations put(Matcher<String> matcher);

    /**
     * Configures an expected POST request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations post(String path);

    /**
     * Configures an expected POST request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations post(Matcher<String> matcher);

    /**
     * Configures an expected DELETE request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations delete(String path);

    /**
     * Configures an expected DELETE request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations delete(Matcher<String> matcher);

    /**
     * Configures an expected PATCH request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations patch(String path);

    /**
     * Configures an expected PATCH request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations patch(Matcher<String> matcher);

    /**
     * Configures an expected OPTIONS request with the specified path.
     *
     * @param path the expected path
     * @return a reference to this configuration
     */
    ProxyExpectations options(String path);

    /**
     * Configures an expected OPTIONS request using the provided matcher to match the path.
     *
     * @param matcher the path matcher
     * @return a reference to this configuration
     */
    ProxyExpectations options(Matcher<String> matcher);
}
