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
package com.stehno.ersatz

import groovy.transform.CompileStatic

import java.util.function.BiFunction
import java.util.function.Consumer

/**
 * Configuration interface for an Ersatz server instance.
 */
@CompileStatic
interface ServerConfig {

    /**
     * Configures the specified feature on the server.
     *
     * @param feature the feature to be configured
     * @return a reference to the server being configured
     */
    ServerConfig feature(ServerFeature feature)

    /**
     * Used to configure HTTP expectations on the server; the provided <code>Consumer<Expectations></code> implementation will have an active
     * <code>Expectations</code> object passed into it for configuring server interaction expectations.
     *
     * @param expects the <code>Consumer<Expectations></code> instance to perform the configuration
     * @return a reference to this server
     */
    ServerConfig expectations(final Consumer<Expectations> expects)

    /**
     * Used to configure HTTP expectations on the server; the provided Groovy <code>Closure</code> will delegate to an <code>Expectations</code>
     * instance for configuring server interaction expectations using the Groovy DSL.
     *
     * @param closure the Groovy <code>Closure</code> which will provide expectation configuration via DSL
     * @return a reference to this server
     */
    ServerConfig expectations(@DelegatesTo(Expectations) final Closure closure)

    /**
     * An alternate means of starting the expectation chain.
     *
     * @return the reference to the Expectation configuration object
     */
    Expectations expects()

    /**
     * Configures the given request content decoder for the specified request content-type. The decoder will be configured globally and used if no
     * overriding decoder is provided during expectation configuration.
     *
     * @param contentType the request content-type
     * @param decoder the request content decoder
     * @return the reference to the server configuration
     */
    ServerConfig decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder)

    /**
     * Configures the given request content decoder for the specified request content-type. The decoder will be configured globally and used if no
     * overriding decoder is provided during expectation configuration.
     *
     * @param contentType the request content-type
     * @param decoder the request content decoder
     * @return the reference to the server configuration
     */
    ServerConfig decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder)

    // FIXME: Add encoders
    // FIXME: global encoder/decoder testing
}
