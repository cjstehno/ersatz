/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz;

import com.stehno.ersatz.cfg.Expectations;
import com.stehno.ersatz.cfg.ServerConfig;
import com.stehno.ersatz.impl.ServerConfigImpl;
import com.stehno.ersatz.server.UnderlyingServer;
import com.stehno.ersatz.server.undertow.UndertowUnderlyingServer;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static java.util.concurrent.TimeUnit.SECONDS;

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
 * See the <a href="http://stehno.com/ersatz/asciidoc/html5/" target="_blank">User Guide</a> for more detailed information.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class ErsatzServer implements Closeable {

    private final UnderlyingServer underlyingServer;
    private final ServerConfigImpl serverConfig;

    public ErsatzServer() {
        this.serverConfig = new ServerConfigImpl(this::start);
        this.underlyingServer = new UndertowUnderlyingServer(serverConfig);
    }

    /**
     * Creates a new Ersatz server instance with either the default configuration or a configuration provided by the Groovy DSL closure.
     *
     * @param closure the configuration closure (delegated to <code>ServerConfig</code>)
     */
    public ErsatzServer(@DelegatesTo(value = ServerConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        this(ConsumerWithDelegate.create(closure));
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
     * Used to retrieve the full URL of the HTTP server.
     *
     * @return the full URL of the HTTP server
     */
    public String getHttpUrl() {
        return getUrl("http", getHttpPort());
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
     * Used to retrieve the full URL of the HTTPS server.
     *
     * @return the full URL of the HTTP server
     */
    public String getHttpsUrl() {
        return getUrl("https", getHttpsPort());
    }

    private String getUrl(final String prefix, final int port){
        if( port > 0 ) {
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
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     * <p>
     * Calling this method when auto-start is enabled will start the server.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    public ErsatzServer expectations(@DelegatesTo(value = Expectations.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return expectations(ConsumerWithDelegate.create(closure));
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

    public ServerConfig timeout(final int value, final TimeUnit units) {
        return serverConfig.timeout(value, units);
    }

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
     * <p>
     * If there are web socket expectations configured, this method will be blocking against the expected operations. Expectations involving web
     * sockets should consider using the timeout parameters - the default is 1s.
     *
     * @param timeout the timeout value (defaults to 1)
     * @param unit    the timeout unit (defaults to SECONDS)
     * @return <code> true</code> if all call criteria were met during test execution.
     */
    public boolean verify(final long timeout, final TimeUnit unit) {
        return serverConfig.getExpectations().verify(timeout, unit);
    }

    public boolean verify(final long timeout) {
        return verify(timeout, SECONDS);
    }

    public boolean verify() {
        return verify(1, SECONDS);
    }
}
