/**
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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.AuthenticationConfig;
import com.stehno.ersatz.cfg.ContentType;
import com.stehno.ersatz.cfg.Expectations;
import com.stehno.ersatz.cfg.ServerConfig;
import com.stehno.ersatz.encdec.DecodingContext;
import com.stehno.ersatz.encdec.RequestDecoders;
import com.stehno.ersatz.encdec.ResponseEncoders;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServerConfigImpl implements ServerConfig {

    private static final int EPHEMERAL_PORT = 0;
    private boolean httpsEnabled;
    private boolean autoStartEnabled = true;
    private boolean mismatchToConsole;
    private URL keystoreLocation;
    private String keystorePass = "ersatz";
    private AuthenticationConfigImpl authenticationConfig;
    private int desiredHttpPort = EPHEMERAL_PORT;
    private int desiredHttpsPort = EPHEMERAL_PORT;
    private final RequestDecoders globalDecoders = new RequestDecoders();
    private final ResponseEncoders globalEncoders = new ResponseEncoders();
    private final ExpectationsImpl expectations = new ExpectationsImpl(globalDecoders, globalEncoders);
    private long timeout;

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

    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    public boolean isAutoStartEnabled() {
        return autoStartEnabled;
    }

    public boolean isMismatchToConsole() {
        return mismatchToConsole;
    }

    public URL getKeystoreLocation() {
        return keystoreLocation;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public AuthenticationConfigImpl getAuthenticationConfig() {
        return authenticationConfig;
    }

    public int getDesiredHttpPort() {
        return desiredHttpPort;
    }

    public int getDesiredHttpsPort() {
        return desiredHttpsPort;
    }

    public ExpectationsImpl getExpectations() {
        return expectations;
    }

    public long getTimeout() {
        return timeout;
    }

    public void clearExpectations(){
        expectations.clear();
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
        this.timeout = MILLISECONDS.convert(value, units);
        return this;
    }

    @Override public ServerConfig timeout(final int value) {
        return timeout(value, SECONDS);
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

    @Override public ServerConfig reportToConsole() {
        return reportToConsole(true);
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

    // Note: that now the CTOR configuration does NOT auto-start the server,
    @Override public ServerConfig expectations(Consumer<Expectations> expects) {
        expects.accept(expectations);
        return this;
    }

    @Override public ServerConfig expectations(Closure closure) {
        return expectations(ConsumerWithDelegate.create(closure));
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

    /**
     * Registers authentication configuration as a Groovy Closure.
     *
     * @param closure the configuration closure
     * @return a reference to this server configuration
     */
    @Override
    public ServerConfig authentication(@DelegatesTo(value = AuthenticationConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return authentication(ConsumerWithDelegate.create(closure));
    }

    /**
     * Registers authentication configuration as a <code>Consumer&lt;AuthenticationConfig&gt;</code>.
     *
     * @param config the configuration Consumer
     * @return a reference to this server configuration
     */
    @Override
    public ServerConfig authentication(final Consumer<AuthenticationConfig> config) {
        authenticationConfig = new AuthenticationConfigImpl();
        config.accept(authenticationConfig);
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
}
