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
package com.stehno.ersatz;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Configuration interface for an Ersatz server instance.
 */
public interface ServerConfig {

    /**
     * Used to control the enabled/disabled state of HTTPS on the server. By default HTTPS is disabled.
     *
     * @return a reference to the server being configured
     */
    ServerConfig https(boolean enabled);

    /**
     * Used to enabled HTTPS on the server. By default HTTPS is disabled.
     *
     * @return a reference to the server being configured
     */
    ServerConfig https();

    /**
     * Used to enable/disable the auto-start feature, which will start the server after any call to either of the <code>expectations</code>
     * configuration methods. With this setting enabled, any other calls to the <code>start()</code> method are ignored. Further configuration is
     * allowed.
     * <p>
     * Auto-start is enabled by default.
     *
     * @param enabled whether or not auto-start is enabled
     * @return a reference to the server being configured
     */
    ServerConfig autoStart(boolean enabled);

    /**
     * Used to enable the auto-start feature, which will start the server after any call to either of the <code>expectations</code> configuration
     * methods. With this setting enabled, any other calls to the <code>start()</code> method are ignored. Further configuration is
     * allowed.
     * <p>
     * Auto-start is enabled by default.
     *
     * @return a reference to the server being configured
     * @deprecated this method is no longer needed and will be removed in 2.0 since auto-start is enabled by default.
     */
    @Deprecated ServerConfig autoStart();

    /**
     * Used to specify the server request timeout property value on the server.
     *
     * @param value the timeout value
     * @param units the units the timeout is specified with
     * @return a reference to the server being configured
     */
    ServerConfig timeout(int value, TimeUnit units);

    /**
     * Used to specify the server request timeout property value on the server, in seconds.
     *
     * @param value the timeout value
     * @return a reference to the server being configured
     */
    ServerConfig timeout(int value);

    /**
     * Causes the mismatched request reports to be generated as console output, rather than only in the logging.
     *
     * @return a reference to the server being configured
     */
    ServerConfig reportToConsole();

    /**
     * Used to toggle the console output of mismatched request reports. By default they are only rendered in the logging. A value of <code>true</code>
     * will cause the report to be output on the console as well.
     *
     * @param toConsole whether or not the report should also be written to the console
     * @return a reference to the server being configured
     */
    ServerConfig reportToConsole(boolean toConsole);

    /**
     * Allows configuration of an external HTTPS keystore with the given location and password. By default, if this is not specified an internally
     * provided keystore will be used for HTTPS certification. See the User Guide for details about configuring your own keystore.
     *
     * @param location the URL of the keystore file
     * @param password the keystore file password
     * @return a reference to the server being configured
     */
    ServerConfig keystore(URL location, String password);

    /**
     * Allows configuration of an external HTTPS keystore with the given location (using the default password "ersatz"). By default, if this is not
     * specified an internally provided keystore will be used for HTTPS certification. See the User Guide for details about configuring your own
     * keystore.
     *
     * @param location the URL of the keystore file
     * @return a reference to the server being configured
     */
    ServerConfig keystore(URL location);

    /**
     * Used to configure HTTP expectations on the server; the provided <code>Consumer<Expectations></code> implementation will have an active
     * <code>Expectations</code> object passed into it for configuring server interaction expectations.
     *
     * @param expects the <code>Consumer<Expectations></code> instance to perform the configuration
     * @return a reference to this server
     */
    ServerConfig expectations(final Consumer<Expectations> expects);

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    ServerConfig expectations(@DelegatesTo(value = Expectations.class, strategy = DELEGATE_FIRST) final Closure closure);

    /**
     * An alternate means of starting the expectation chain.
     *
     * @return the reference to the Expectation configuration object
     */
    Expectations expects();

    /**
     * Configures the given request content decoder for the specified request content-type. The decoder will be configured globally and used if no
     * overriding decoder is provided during expectation configuration.
     *
     * @param contentType the request content-type
     * @param decoder     the request content decoder
     * @return the reference to the server configuration
     */
    ServerConfig decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder);

    /**
     * Configures the given request content decoder for the specified request content-type. The decoder will be configured globally and used if no
     * overriding decoder is provided during expectation configuration.
     *
     * @param contentType the request content-type
     * @param decoder     the request content decoder
     * @return the reference to the server configuration
     */
    ServerConfig decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder);

    /**
     * Registers a global response body encoder.
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param objectType the response object type to be encoded
     * @param encoder    the encoder function
     * @return a reference to this server configuration
     */
    ServerConfig encoder(String contentType, Class objectType, Function<Object, String> encoder);

    /**
     * Registers a global response body encoder.
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param objectType the response object type to be encoded
     * @param encoder    the encoder function
     * @return a reference to this server configuration
     */
    ServerConfig encoder(ContentType contentType, Class objectType, Function<Object, String> encoder);

    /**
     * Registers authentication configuration as a Groovy Closure.
     *
     * @param closure the configuration closure
     * @return a reference to this server configuration
     */
    ServerConfig authentication(@DelegatesTo(value = AuthenticationConfig.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Registers authentication configuration as a <code>Consumer<AuthenticationConfig></code>.
     *
     * @param config the configuration Consumer
     * @return a reference to this server configuration
     */
    ServerConfig authentication(Consumer<AuthenticationConfig> config);

    /**
     * Allows the specific configuration of the HTTP server port. The default ephemeral port should be used in most cases since
     * specifying the port will negate the ability to run tests in parallel and will also allow possible collisions with
     * other running servers on the host.
     * <p>
     * Do NOT specify this setting unless you really need to.
     */
    ServerConfig httpPort(int value);

    /**
     * Allows the specific configuration of the HTTPS server port. The default ephemeral port should be used in most cases since
     * specifying the port will negate the ability to run tests in parallel and will also allow possible collisions with
     * other running servers on the host.
     * <p>
     * Do NOT specify this setting unless you really need to.
     */
    ServerConfig httpsPort(int value);
}
