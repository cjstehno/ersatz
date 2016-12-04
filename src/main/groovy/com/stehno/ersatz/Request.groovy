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

import java.util.function.Consumer
import java.util.function.Function

/**
 * Configuration interface for HTTP requests.
 */
@CompileStatic
interface Request {

    // TODO: documentation notes
    // Note that headers are matched as "contains" - others?
    // Note how multiple responses work

    Request header(final String name, final String value)

    String getHeader(final String name)

    Request contentType(final String contentType)

    Request query(final String name, final String value)

    List<String> getQuery(final String name)

    Request cookie(final String name, final String value)

    String getCookie(final String name)

    /**
     * Specifies a listener which will be called with the active request whenever this request is matched at test-time.
     *
     * @param listener the request call listener
     * @return a reference to this request
     */
    Request listener(final Consumer<Request> listener)

    /**
     * Allows the specification of a custom call verifier so that the number of times the request is called may be matched. See the
     * <code>Verifiers</code> class for available implementations.
     *
     * @param verifier the verifier to be used
     * @return a reference to this request
     */
    Request verifier(final Function<Integer, Boolean> verifier)

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

    /**
     * Allows for additional configuration of request matching criteria. The provided <code>Function<Request,Boolean></code> will have the active
     * request passed into it and the function will return a value of <code>true</code> if the condition is met. All of the standard matching
     * criteria for the request will still be verified.
     *
     * Multiple additional conditions may be applied.
     *
     * @param matcher the matching function to be added.
     * @return a reference to this request
     */
    Request condition(final Function<Request, Boolean> matcher)
}

/*
    TODO:
        - headers(map)
        - query(map)
        - cookies(map)
 */
