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
package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.cfg.Expectations;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.cfg.WaitFor;
import io.github.cjstehno.ersatz.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.server.UnderlyingServer;
import io.github.cjstehno.ersatz.server.undertow.UndertowUnderlyingServer;
import lombok.Getter;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.cfg.WaitFor.atMost;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PROTECTED;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The main entry point for configuring an Ersatz server, which allows configuring of the expectations and management of the server itself. This is
 * the class that should be instantiated in unit tests.
 * <p>
 * The server will be started on an ephemeral port so as not to collide with itself or other server applications running in the test environment. In
 * your tests, you can retrieve the server port or URL using the <code>getPort()</code> and <code>getServerUrl()</code> methods respectively.
 * <p>
 * Using the <code>ErsatzServer</code> follows the workflow:
 *
 * <ol>
 *     <li>Create the <code>ErsatzServer</code> instance.</li>
 *     <li>Configure the expectations.</li>
 *     <li>Start the server</li>
 *     <li>Run your client tests against the server.</li>
 *     <li>Verify the expectations.</li>
 *     <li>Stop the server.</li>
 * </ol>
 * <p>
 * See the <a href="http://cjstehno.github.io/ersatz/asciidoc/html5/" target="_blank">User Guide</a> for more detailed information.
 */
public class ErsatzServer implements Closeable {

    private final UnderlyingServer underlyingServer;
    @Getter(PROTECTED) private final ServerConfigImpl serverConfig;

    /**
     * Creates a new Ersatz server instance with empty (default) configuration.
     */
    public ErsatzServer() {
        this(new ServerConfigImpl());
    }

    /**
     * Creates a new Ersatz server instance with the provided configuration.
     *
     * @param config the configuration instance.
     */
    public ErsatzServer(final ServerConfig config) {
        this.serverConfig = (ServerConfigImpl) config;
        this.serverConfig.setStarter(this::start);

        this.underlyingServer = new UndertowUnderlyingServer(serverConfig);
    }

    /**
     * Creates a new Ersatz server instance configured by the provided <code>Consumer</code>, which will have an instance of <code>ServerConfig</code>
     * passed into it for server configuration.
     *
     * @param consumer the configuration consumer
     */
    public ErsatzServer(final Consumer<ServerConfig> consumer) {
        this();
        if (consumer != null) {
            consumer.accept(serverConfig);
        }
    }

    /**
     * Used to retrieve the port where the HTTP server is running.
     *
     * @return the HTTP port
     */
    public int getHttpPort() {
        return underlyingServer.getActualHttpPort();
    }

    /**
     * Used to retrieve the port where the HTTPS server is running.
     *
     * @return the HTTPS port
     */
    public int getHttpsPort() {
        return underlyingServer.getActualHttpsPort();
    }

    /**
     * Used to retrieve the HTTP or HTTPS port where the server is running, based on the value of the boolean passed in.
     * This method is useful for test cases where the SSL state is parameterized.
     *
     * @param https whether the retrieved port is for HTTPS.
     * @return the port value, or -1 if not available.
     */
    public int getPort(final boolean https) {
        return https ? getHttpsPort() : getHttpPort();
    }

    /**
     * Used to retrieve whether HTTPS is enabled or not.
     *
     * @return true if HTTPS is enabled
     */
    public boolean isHttpsEnabled() {
        return serverConfig.isHttpsEnabled();
    }

    /**
     * Used to retrieve the full URL of the HTTP server.
     *
     * @return the full URL of the HTTP server
     */
    public String getHttpUrl() {
        return getUrl("http", getHttpPort());
    }

    /**
     * Used to retrieve the full URL of the HTTPS server.
     *
     * @return the full URL of the HTTP server
     */
    public String getHttpsUrl() {
        return getUrl("https", getHttpsPort());
    }

    /**
     * Used to retrieve the Web Socket URL.
     *
     * @return the web socket URL
     */
    public String getWsUrl() {
        return getUrl("ws", getHttpPort());
    }

    /**
     * Used to retrieve the Web Socket URL with the appended path.
     *
     * @param path the path to be appended to the web socket url
     * @return the web socket url with the appended path
     */
    public String wsUrl(final String path) {
        return getWsUrl() + path;
    }

    /**
     * Used to retrieve the full url of the server, either HTTP or HTTPS, based on the passed in boolean value. This
     * method is useful for testing in cases where the HTTPS enablement is parameterized.
     * <p>
     * If the scheme is not available, the port value in the URL will be -1.
     *
     * @param https whether the retrieved URL is for HTTPS or not
     * @return the full url of the server for the scheme (HTTPS or HTTP).
     */
    public String getUrl(final boolean https) {
        return https ? getHttpsUrl() : getHttpUrl();
    }

    private static String getUrl(final String prefix, final int port) {
        if (port > 0) {
            return prefix + "://localhost:" + port;
        } else {
            throw new IllegalStateException("The port (" + port + ") is invalid: Has the server been started?");
        }
    }

    /**
     * A helper method which may be used to append the given path to the server HTTP url.
     *
     * @param path the path to be applied
     * @return the resulting URL
     */
    public String httpUrl(final String path) {
        return getHttpUrl() + path;
    }

    /**
     * A helper method which may be used to append the given path to the server HTTPS url.
     *
     * @param path the path to be applied
     * @return the resulting URL
     */
    public String httpsUrl(final String path) {
        return getHttpsUrl() + path;
    }

    /**
     * Used to configure HTTP expectations on the server; the provided <code>Consumer&lt;Expectations&gt;</code> implementation will have an active
     * <code>Expectations</code> object passed into it for configuring server interaction expectations.
     * <p>
     * Calling this method when auto-start is enabled will start the server.
     *
     * @param expects the <code>Consumer&lt;Expectations&gt;</code> instance to perform the configuration
     * @return a reference to this server
     */
    public ErsatzServer expectations(final Consumer<Expectations> expects) {
        serverConfig.expectations(expects);

        if (serverConfig.isAutoStartEnabled()) {
            underlyingServer.start();
        }

        return this;
    }

    /**
     * An alternate means of starting the expectation chain.
     * <p>
     * Calling this method when auto-start is enabled will <b>NOT</b> start the server. Use one of the other expectation configuration method if
     * auto-start functionality is desired.
     *
     * @return the reference to the Expectation configuration object
     */
    public Expectations expects() {
        return serverConfig.expects();
    }

    /**
     * Used to specify the server request timeout property value on the server.
     * <p>
     * The IDLE_TIMEOUT, NO_REQUEST_TIMEOUT, REQUEST_PARSE_TIMEOUT, READ_TIMEOUT and WRITE_TIMEOUT are all configured to the same specified
     * value.
     *
     * @param value the timeout value
     * @param units the units the timeout is specified with
     * @return a reference to the server being configured
     */
    public ServerConfig timeout(final int value, final TimeUnit units) {
        return serverConfig.timeout(value, units);
    }

    /**
     * Used to specify the server request timeout property value on the server (in seconds).
     * <p>
     * The IDLE_TIMEOUT, NO_REQUEST_TIMEOUT, REQUEST_PARSE_TIMEOUT, READ_TIMEOUT and WRITE_TIMEOUT are all configured to the same specified
     * value.
     *
     * @param value the timeout value
     * @return a reference to the server being configured
     */
    public ServerConfig timeout(final int value) {
        return timeout(value, SECONDS);
    }

    /**
     * Used to start the HTTP server for test interactions. This method should be called after configuration of expectations and before the test
     * interactions are executed against the server.
     *
     * @return a reference to this server
     */
    public ErsatzServer start() {
        underlyingServer.start();
        return this;
    }

    /**
     * Clears all configured expectations from the server. Does not affect global encoders or decoders.
     */
    public void clearExpectations() {
        serverConfig.clearExpectations();
    }

    /**
     * Used to stop the HTTP server. The server may be restarted after it has been stopped.
     */
    public void stop() {
        underlyingServer.stop();
    }

    /**
     * An alias to the <code>stop()</code> method.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Used to verify that all of the expected request interactions were called the appropriate number of times. This method should be called after
     * all test interactions have been performed. This is an optional step since generally you will also be receiving the expected response back
     * from the server; however, this verification step can come in handy when simply needing to know that a request is actually called or not.
     *
     * @param timeout the timeout value
     * @param unit    the timeout unit
     * @return <code>true</code> if all call criteria were met during test execution.
     * @deprecated Use the <code>verify(WaitFor)</code> version instead
     */
    @Deprecated(since = "4.0.0", forRemoval = true)
    public boolean verify(final long timeout, final TimeUnit unit) {
        return verify(atMost(timeout, unit));
    }

    /**
     * Used to verify that all of the expected request interactions were called the appropriate number of times. This method should be called after
     * all test interactions have been performed. This is an optional step since generally you will also be receiving the expected response back
     * from the server; however, this verification step can come in handy when simply needing to know that a request is actually called or not.
     *
     * @param waitFor the timeout waiting value
     * @return <code>true</code> if all call criteria were met during test execution.
     */
    public boolean verify(final WaitFor waitFor) {
        return serverConfig.getExpectations().verify(waitFor);
    }

    /**
     * Used to verify that all of the expected request interactions were called the appropriate number of times. This method should be called after
     * all test interactions have been performed. This is an optional step since generally you will also be receiving the expected response back
     * from the server; however, this verification step can come in handy when simply needing to know that a request is actually called or not.
     *
     * @param timeout the timeout value (in seconds)
     * @return <code>true</code> if all call criteria were met during test execution.
     * @deprecated Use the <code>verify(WaitFor)</code> version instead
     */
    @Deprecated(since = "4.0.0", forRemoval = true)
    public boolean verify(final long timeout) {
        return verify(timeout, SECONDS);
    }

    /**
     * Used to verify that all of the expected request interactions were called the appropriate number of times. This method should be called after
     * all test interactions have been performed. This is an optional step since generally you will also be receiving the expected response back
     * from the server; however, this verification step can come in handy when simply needing to know that a request is actually called or not.
     *
     * @return <code>true</code> if all call criteria were met during test execution.
     */
    public boolean verify() {
        return verify(1, SECONDS);
    }

    /**
     * Helper method to wrap a call to the <code>verify()</code> method within a JUnit <code>assertTrue(...)</code> call.
     *
     * This method applies a 1 second waiting time before timing out.
     */
    public void assertVerified() {
        assertVerified(WaitFor.ONE_SECOND);
    }

    /**
     * Helper method to wrap a call to the <code>verify(timeout, unit)</code> method within a JUnit <code>assertTrue(...)</code> call.
     *
     * @param waitFor the amount of time the verification should wait before considering a timeout.
     */
    public void assertVerified(final WaitFor waitFor) {
        assertTrue(verify(waitFor), "The server expectation verification failed.");
    }
}
