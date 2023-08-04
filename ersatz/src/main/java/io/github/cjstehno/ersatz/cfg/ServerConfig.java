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

import io.github.cjstehno.ersatz.encdec.DecodingContext;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Configuration interface for an Ersatz server instance.
 */
public interface ServerConfig {

    /**
     * Used to control the enabled/disabled state of HTTPS on the server. By default HTTPS is disabled.
     *
     * @param enabled whether or not HTTPS support is enabled
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
    default ServerConfig timeout(int value) {
        return timeout(value, SECONDS);
    }

    /**
     * Causes the mismatched request reports to be generated as console output, rather than only in the logging.
     *
     * @return a reference to the server being configured
     */
    default ServerConfig reportToConsole() {
        return reportToConsole(true);
    }

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
     * Used to configure HTTP expectations on the server; the provided <code>Consumer&lt;Expectations&gt;</code> implementation will have an active
     * <code>Expectations</code> object passed into it for configuring server interaction expectations.
     *
     * If auto-start is enabled (default) the server will be started after the expectations are applied.
     *
     * @param expects the <code>Consumer&lt;Expectations&gt;</code> instance to perform the configuration
     * @return a reference to this server
     */
    ServerConfig expectations(Consumer<Expectations> expects);

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
    ServerConfig decoder(String contentType, BiFunction<byte[], DecodingContext, Object> decoder);

    /**
     * Configures the given request content decoder for the specified request content-type. The decoder will be configured globally and used if no
     * overriding decoder is provided during expectation configuration.
     *
     * @param contentType the request content-type
     * @param decoder     the request content decoder
     * @return the reference to the server configuration
     */
    default ServerConfig decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        return decoder(contentType.getValue(), decoder);
    }

    /**
     * Registers a global response body encoder.
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param contentType the response content type to be encoded
     * @param objectType the response object type to be encoded
     * @param encoder    the encoder function
     * @return a reference to this server configuration
     */
    ServerConfig encoder(String contentType, Class objectType, Function<Object, byte[]> encoder);

    /**
     * Registers a global response body encoder.
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param contentType the response content type to be encoded
     * @param objectType the response object type to be encoded
     * @param encoder    the encoder function
     * @return a reference to this server configuration
     */
    default ServerConfig encoder(ContentType contentType, Class objectType, Function<Object, byte[]> encoder) {
        return encoder(contentType.getValue(), objectType, encoder);
    }

    /**
     * Allows the specific configuration of the HTTP server port. The default ephemeral port should be used in most cases since
     * specifying the port will negate the ability to run tests in parallel and will also allow possible collisions with
     * other running servers on the host.
     * <p>
     * Do NOT specify this setting unless you really need to.
     *
     * @param value the desired HTTP port
     * @return a reference to this server configuration
     */
    ServerConfig httpPort(int value);

    /**
     * Allows the specific configuration of the HTTPS server port. The default ephemeral port should be used in most cases since
     * specifying the port will negate the ability to run tests in parallel and will also allow possible collisions with
     * other running servers on the host.
     * <p>
     * Do NOT specify this setting unless you really need to.
     *
     * @param value the desired HTTPS port
     * @return a reference to this server configuration
     */
    ServerConfig httpsPort(int value);

    /**
     * Causes the full response content to be rendered in the server log message (if true) for the cases where the
     * response content is a renderable textual response. If false (or binary response) only the number of bytes and
     * content type will be rendered for the response.
     *
     * @param value whether or not to enable logging of response content (false by default)
     * @return a reference to this server configuration.
     */
    ServerConfig logResponseContent(boolean value);

    /**
     * Causes the full response content to be rendered in the server log message for the cases where the response
     * content is a renderable textual response. If false (or binary response) only the number of bytes and content type
     * will be rendered for the response.
     *
     * @return a reference to this server configuration.
     */
    default ServerConfig logResponseContent() {
        return logResponseContent(true);
    }

    /**
     * Allows the configuration of the number of IO and Worker threads to be used by the underlying server.
     *
     * @param io the number of IO threads (should be fewer than workers; default is 2)
     * @param worker the number of worker threads (should be more than io; default is 16)
     * @return a reference to this server configuration
     */
    ServerConfig serverThreads(int io, int worker);

    /**
     * Allows the configuration of the number of IO threads to be used by the underlying server. The worker threads will
     * be configured to 8 times the configured io number.
     *
     * @param io the number of IO threads (should be a small number; default is 2)
     * @return a reference to this server configuration
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    default ServerConfig serverThreads(final int io) {
        return serverThreads(io, io * 8);
    }

    /**
     * Allows the configuration of any global request requirements.
     *
     * @param requirements the requirements configuration consumer
     * @return a reference to this server configuration
     */
    ServerConfig requirements(Consumer<Requirements> requirements);
}
