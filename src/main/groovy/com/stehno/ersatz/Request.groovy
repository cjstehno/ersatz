/*
 * Copyright (C) 2016 Christopher J. Stehno
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
import org.hamcrest.Matcher

import java.util.function.Consumer

/**
 * Configuration interface for HTTP request expectations.
 */
@CompileStatic
interface Request {

    /**
     * Specifies a request header to be configured in the expected request.
     *
     * @param name the header name
     * @param value the header value
     * @return this request
     */
    Request header(final String name, final Object value)

    /**
     * Specifies request headers as a Map of names to values to be configured in the expected request.
     *
     * @param heads the map of headers
     * @return this request
     */
    Request headers(final Map<String, Object> heads)

    /**
     * Used to specify a request query parameter to be configured in the expected request. As per the HTTP spec, the query string parameters may be
     * specified multiple times with different values to denote a parameter with multiple values.
     *
     * @param name the parameter name
     * @param value the parameter value
     * @return this request
     */
    Request query(final String name, final Object value)

    /**
     * Used to specify a map of request query parameters for configuration on the expected request.
     *
     * @param map the map of query parameters
     * @return this request
     */
    Request queries(final Map<String, Object> map)

    /**
     * Specifies a request cookie to be configured with the given name and value.
     *
     * @param name the cookie name
     * @param value the cookie value
     * @return this request
     */
    Request cookie(final String name, final Object value)

    /**
     * Used to configure a map of cookies on the request.
     *
     * @param cookies the map of cookies
     * @return this request
     */
    Request cookies(final Map<String, Object> cookies)

    /**
     * Specifies a listener which will be called with the active request whenever this request is matched at test-time.
     *
     * @param listener the request call listener
     * @return a reference to this request
     */
    Request listener(final Consumer<ClientRequest> listener)

    /**
     * Allows the specification of a custom call verifier so that the number of times the request is called may be matched. See the
     * <code>Verifiers</code> class for available implementations.
     *
     * @param verifier the verifier to be used
     * @return a reference to this request
     */
    Request called(final Matcher<Integer> callVerifier)

    /**
     * Initiates the definition of a response for this request.
     *
     * @return a response for this request, for configuration
     */
    Response responds()

    /**
     * Allows for configuration of a <code>Response</code> by the given <code>Consumer</code>, which will have a <code>Response</code> object passed
     * into it.
     *
     * @param responder the <code>Consumer<Response></code> to provide configuration of the response
     * @return a reference to this request
     */
    Request responder(final Consumer<Response> responder)

    /**
     * Allows for configuration of a <code>Response</code> by the given Groovy <code>Closure</code>, which will delegate to a <code>Response</code>
     * instance passed into it for configuration using the Groovy DSL.
     *
     * @param closure the <code>Consumer<Response></code> to provide configuration of the response
     * @return a reference to this request
     */
    Request responder(@DelegatesTo(Response) final Closure closure)
}