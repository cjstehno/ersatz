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
import org.hamcrest.Matcher

import java.util.function.Consumer

/**
 * The <code>Expectations</code> interface is the root element of the expectation configuration, which provides the ability to define request
 * expectations and responses for test interactions.
 *
 * Internal expectation matching is done using <a href="http://hamcrest.org/" target="_blank">Hamcrest</a> <code>Matcher</code>s - the methods
 * without explicit Matches provide one as a convenience based on the property and value type (see method description). All configured matchers must
 * match for a specific expectation to be considered a match and if there are multiple matching expectations, the first one configured will be the
 * one considered as the match.
 */
@CompileStatic @SuppressWarnings('MethodCount')
interface Expectations {

    /**
     * Allows configuration of a request expectation matching any request method.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    Request any(String path)

    /**
     * Allows configuration of a request expectation matching any request method.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    Request any(Matcher<String> matcher)

    /**
     * Allows configuration of a request expectation matching any request method using the Groovy DSL.
     *
     * @param path the expected request path.
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request any(String path, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a request expectation matching any request method using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request any(Matcher<String> matcher, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<Request></code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request any(String path, Consumer<Request> consumer)

    /**
     * Allows configuration of request expectation matching any request method using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<Request></code> will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request any(Matcher<String> matcher, Consumer<Request> consumer)

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    Request get(String path)

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param matcher the path matcher.
     * @return a <code>Request</code> configuration object
     */
    Request get(Matcher<String> matcher)

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param path the expected request path.
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request get(String path, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request get(Matcher<String> matcher, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request get(String path, Consumer<Request> config)

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request get(Matcher<String> matcher, Consumer<Request> config)

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    Request head(String path)

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    Request head(Matcher<String> matcher)

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request head(String path, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request head(Matcher<String> matcher, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request head(String path, Consumer<Request> config)

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request head(Matcher<String> matcher, Consumer<Request> config)

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param path the request path.
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent post(String path)

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent post(Matcher<String> matcher)

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent post(String path, @DelegatesTo(RequestWithContent) Closure closure)

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent post(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure)

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent post(String path, Consumer<RequestWithContent> config)

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent post(Matcher<String> matcher, Consumer<RequestWithContent> config)

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent put(String path)

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent put(Matcher<String> matcher)

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent put(String path, @DelegatesTo(RequestWithContent) Closure closure)

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent put(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure)

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent put(String path, Consumer<RequestWithContent> config)

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent put(Matcher<String> matcher, Consumer<RequestWithContent> config)

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    Request delete(String path)

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    Request delete(Matcher<String> matcher)

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request delete(String path, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request delete(Matcher<String> matcher, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request delete(String path, Consumer<Request> config)

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request delete(Matcher<String> matcher, Consumer<Request> config)

    /**
     * Allows configuration of a PATCH request expectation.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent patch(String path)

    /**
     * Allows configuration of a PATCH request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent patch(Matcher<String> matcher)

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent patch(String path, @DelegatesTo(RequestWithContent) Closure closure)

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent patch(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure)

    /**
     * Allows configuration of a PATCH request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent patch(String path, Consumer<RequestWithContent> config)

    /**
     * Allows configuration of a PATCH request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent patch(Matcher<String> matcher, Consumer<RequestWithContent> config)

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    Request options(String path)

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    Request options(Matcher<String> matcher)

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request options(String path, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @pram closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    Request options(Matcher<String> matcher, @DelegatesTo(Request) Closure closure)

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code>
     * will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request options(String path, Consumer<Request> config)

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code>
     * will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @pram config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request options(Matcher<String> matcher, Consumer<Request> config)
}
