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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.ContentType;
import io.github.cjstehno.ersatz.cfg.Expectations;
import io.github.cjstehno.ersatz.cfg.Requirements;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.encdec.DecodingContext;
import io.github.cjstehno.ersatz.encdec.RequestDecoders;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import lombok.Getter;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation of the <code>ServerConfig</code> interface.
 */
public class ServerConfigImpl implements ServerConfig {

    private static final int EPHEMERAL_PORT = 0;
    private boolean httpsEnabled;
    private boolean autoStartEnabled = true;
    private boolean mismatchToConsole;
    private URL keystoreLocation;
    private String keystorePass = "ersatz";
    private int desiredHttpPort = EPHEMERAL_PORT;
    private int desiredHttpsPort = EPHEMERAL_PORT;
    private final RequestDecoders globalDecoders = new RequestDecoders();
    private final ResponseEncoders globalEncoders = new ResponseEncoders();
    @Getter private final ExpectationsImpl expectations;
    @Getter private final RequirementsImpl requirements;
    private Runnable starter;
    private long timeout;
    private boolean logResponseContent;
    private int ioThreads = 2;
    private int workerThreads = 16;

    /**
     * Creates a new empty configuration instance.
     */
    public ServerConfigImpl() {
        this.expectations = new ExpectationsImpl(globalEncoders, globalDecoders);
        this.requirements = new RequirementsImpl();
    }

    /**
     * Used to inject the server-starter handle.
     *
     * @param starter the server-starter handle to be used
     */
    public void setStarter(final Runnable starter) {
        this.starter = starter;
    }

    /**
     * Used to control the enabled/disabled state of HTTPS on the server. By default HTTPS is disabled.
     *
     * @param enabled optional toggle value (true if not specified)
     * @return a reference to the server being configured
     */
    @Override public ServerConfig https(boolean enabled) {
        httpsEnabled = enabled;
        return this;
    }

    @Override public ServerConfig https() {
        return https(true);
    }

    /**
     * Whether or not the https support is enabled. Defaults to <code>false</code>.
     *
     * @return true, if the https support is enabled
     */
    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    /**
     * Whether or not the auto-start condition is enabled. Defaults to <code>true</code>.
     *
     * @return true, if the auto-start condition is enabled.
     */
    public boolean isAutoStartEnabled() {
        return autoStartEnabled;
    }

    /**
     * Whether or not the mismatched request conditions should be logged to the console. Defaults to <code>false</code>.
     *
     * @return true, if the mismatched request conditions should be logged to the console
     */
    public boolean isMismatchToConsole() {
        return mismatchToConsole;
    }

    /**
     * Retrieves the location for the HTTPS keystore.
     *
     * @return the keystore location (URL)
     */
    public URL getKeystoreLocation() {
        return keystoreLocation;
    }

    /**
     * Retrieves the HTTPS keystore password.
     *
     * @return the keystore password
     */
    public String getKeystorePass() {
        return keystorePass;
    }

    /**
     * Retrieves the configured (desired) HTTP port - this may be overridden by the server itself.
     *
     * @return the HTTP port
     */
    public int getDesiredHttpPort() {
        return desiredHttpPort;
    }

    /**
     * Retrieves the configured (desired) HTTPS port - this may be overridden by the server itself.
     *
     * @return the HTTPS port
     */
    public int getDesiredHttpsPort() {
        return desiredHttpsPort;
    }

    /**
     * Retrieves the configured timeout value for server requests.
     *
     * @return the server timeout value
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Used to clear out the configured expectations and requirements.
     */
    public void clearExpectations() {
        expectations.clear();
        requirements.clear();
    }

    /**
     * Whether or not the response content should be logged for each request.
     *
     * @return true, if the response content should be logged
     */
    public boolean isLogResponseContent() {
        return logResponseContent;
    }

    /**
     * Retrieves the IO threads configuration setting. Defaults to 2.
     *
     * @return the number of IO threads
     */
    public int getIoThreads() {
        return ioThreads;
    }

    /**
     * Retrieves the number of Worker threads configured. Defaults to 8.
     *
     * @return the number of worker threads
     */
    public int getWorkerThreads() {
        return workerThreads;
    }

    /**
     * Used to enable/disable the auto-start feature, which will start the server after any call to either of the <code>expectations</code>
     * configuration methods. With this setting enabled, any other calls to the <code>start()</code> method are ignored. Further configuration is
     * allowed.
     * <p>
     * Auto-start is enabled by default.
     *
     * @param autoStart whether or not auto-start is enabled
     * @return a reference to the server being configured
     */
    @Override public ServerConfig autoStart(boolean autoStart) {
        autoStartEnabled = autoStart;
        return this;
    }

    /**
     * Used to specify the server request timeout property value on the server. If not specified, <code>SECONDS</code> will be used as the units.
     * <p>
     * The IDLE_TIMEOUT, NO_REQUEST_TIMEOUT, REQUEST_PARSE_TIMEOUT, READ_TIMEOUT and WRITE_TIMEOUT are all configured to the same specified
     * value.
     *
     * @param value the timeout value
     * @param units the units the timeout is specified with (or <code>SECONDS</code>)
     * @return a reference to the server being configured
     */
    @Override public ServerConfig timeout(final int value, final TimeUnit units) {
        this.timeout = units.toMillis(value);
        return this;
    }

    /**
     * Used to toggle the console output of mismatched request reports. By default they are only rendered in the logging. A value of <code>true</code>
     * will cause the report to be output on the console as well.
     *
     * @param toConsole whether or not the report should also be written to the console
     * @return a reference to the server being configured
     */
    @Override
    public ServerConfig reportToConsole(boolean toConsole) {
        mismatchToConsole = toConsole;
        return this;
    }

    /**
     * Allows configuration of an external HTTPS keystore with the given location and password. By default, if this is not specified an internally
     * provided keystore will be used for HTTPS certification. See the User Guide for details about configuring your own keystore.
     *
     * @param location the URL of the keystore file
     * @param password the keystore file password (defaults to "ersatz" if omitted)
     * @return a reference to the server being configured
     */
    @Override public ServerConfig keystore(final URL location, final String password) {
        keystoreLocation = location;
        keystorePass = password;
        return this;
    }

    @Override public ServerConfig keystore(final URL location) {
        return keystore(location, "ersatz");
    }

    @Override public ServerConfig expectations(final Consumer<Expectations> expects) {
        expects.accept(expectations);

        if (autoStartEnabled) {
            starter.run();
        }

        return this;
    }

    @Override public Expectations expects() {
        return expectations;
    }

    @Override
    public ServerConfig decoder(String contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        globalDecoders.register(contentType, decoder);
        return this;
    }

    @Override
    public ServerConfig decoder(ContentType contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        return decoder(contentType.getValue(), decoder);
    }

    @Override public ServerConfig encoder(String contentType, Class objectType, Function<Object, byte[]> encoder) {
        globalEncoders.register(contentType, objectType, encoder);
        return this;
    }

    @Override
    public ServerConfig httpPort(int serverPort) {
        desiredHttpPort = serverPort;
        return this;
    }

    @Override
    public ServerConfig httpsPort(int serverPort) {
        desiredHttpsPort = serverPort;
        return this;
    }

    @Override public ServerConfig logResponseContent(boolean value) {
        logResponseContent = value;
        return this;
    }

    @Override public ServerConfig serverThreads(int io, int worker) {
        ioThreads = io;
        workerThreads = worker;
        return this;
    }

    @Override public ServerConfig requirements(final Consumer<Requirements> requires) {
        requires.accept(requirements);
        return this;
    }
}
