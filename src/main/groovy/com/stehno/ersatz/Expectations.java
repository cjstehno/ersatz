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

import com.stehno.ersatz.impl.AnyExpectations;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.hamcrest.Matcher;

import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * The <code>Expectations</code> interface is the root element of the expectation configuration, which provides the ability to define request
 * expectations and responses for test interactions.
 * <p>
 * Internal expectation matching is done using <a href="http://hamcrest.org/" target="_blank">Hamcrest</a> <code>Matcher</code>s - the methods
 * without explicit Matches provide one as a convenience based on the property and value type (see method description). All configured matchers must
 * match for a specific expectation to be considered a match and if there are multiple matching expectations, the first one configured will be the
 * one considered as the match.
 */
public interface Expectations extends AnyExpectations {

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request get(String path);

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    Request GET(String path);

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param matcher the path matcher.
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request get(Matcher<String> matcher);

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param matcher the path matcher.
     * @return a <code>Request</code> configuration object
     */
    Request GET(Matcher<String> matcher);

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request get(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request GET(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request get(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request GET(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request get(String path, Consumer<Request> config);

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request GET(String path, Consumer<Request> config);

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request get(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request GET(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request head(String path);

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    Request HEAD(String path);

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request head(Matcher<String> matcher);

    /**
     * Allows configuration of a HEAD request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    Request HEAD(Matcher<String> matcher);

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request head(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request HEAD(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request head(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request HEAD(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request head(String path, Consumer<Request> config);

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request HEAD(String path, Consumer<Request> config);

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request head(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Allows configuration of a HEAD request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request HEAD(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param path the request path.
     * @return a <code>RequestWithContent</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent post(String path);

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param path the request path.
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent POST(String path);

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent post(Matcher<String> matcher);

    /**
     * Allows configuration of a POST request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent POST(Matcher<String> matcher);

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    RequestWithContent post(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    RequestWithContent POST(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    RequestWithContent post(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    RequestWithContent POST(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent post(String path, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent POST(String path, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent post(Matcher<String> matcher, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a POST request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent POST(Matcher<String> matcher, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent put(String path);

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent PUT(String path);

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent put(Matcher<String> matcher);

    /**
     * Allows configuration of a PUT request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent PUT(Matcher<String> matcher);

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    RequestWithContent put(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    RequestWithContent PUT(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    RequestWithContent put(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    RequestWithContent PUT(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent put(String path, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent PUT(String path, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent put(Matcher<String> matcher, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a PUT request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent PUT(Matcher<String> matcher, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request delete(String path);

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    Request DELETE(String path);

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request delete(Matcher<String> matcher);

    /**
     * Allows configuration of a DELETE request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    Request DELETE(Matcher<String> matcher);

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request delete(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request DELETE(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request delete(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request DELETE(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request delete(String path, Consumer<Request> config);

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request DELETE(String path, Consumer<Request> config);

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request delete(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Allows configuration of a DELETE request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request DELETE(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Allows configuration of a PATCH request expectation.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent patch(String path);

    /**
     * Allows configuration of a PATCH request expectation.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent PATCH(String path);

    /**
     * Allows configuration of a PATCH request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent patch(Matcher<String> matcher);

    /**
     * Allows configuration of a PATCH request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     */
    RequestWithContent PATCH(Matcher<String> matcher);

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    RequestWithContent patch(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    RequestWithContent PATCH(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    RequestWithContent patch(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    RequestWithContent PATCH(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a PATCH request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent patch(String path, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a PATCH request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent PATCH(String path, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a PATCH request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) RequestWithContent patch(Matcher<String> matcher, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a PATCH request expectation using the provided <code>Consumer<Request></code>. The
     * <code>Consumer<RequestWithContent></code> will have an instance of <code>RequestWithContent</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>RequestWithContent</code> configuration object
     * @param config the configuration consumer
     */
    RequestWithContent PATCH(Matcher<String> matcher, Consumer<RequestWithContent> config);

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request options(String path);

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param path the expected request path.
     * @return a <code>Request</code> configuration object
     */
    Request OPTIONS(String path);

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    @Deprecated(since = "2.0", forRemoval = true) Request options(Matcher<String> matcher);

    /**
     * Allows configuration of a OPTIONS request expectation.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     */
    Request OPTIONS(Matcher<String> matcher);

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request options(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request OPTIONS(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Request options(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param closure the Groovy closure containing the configuration
     */
    Request OPTIONS(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code>
     * will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request options(String path, Consumer<Request> config);

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code>
     * will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request OPTIONS(String path, Consumer<Request> config);

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code>
     * will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    @Deprecated(since = "2.0", forRemoval = true) Request options(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Allows configuration of a OPTIONS request expectation using the provided <code>Consumer<Request></code>. The <code>Consumer<Request></code>
     * will have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @return a <code>Request</code> configuration object
     * @param config the configuration consumer
     */
    Request OPTIONS(Matcher<String> matcher, Consumer<Request> config);

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection will be expected in order
     * for the verification to pass.
     */
    WebSocketExpectations ws(String path);

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection will be expected in order
     * for the verification to pass.
     */
    WebSocketExpectations ws(String path, @DelegatesTo(value = WebSocketExpectations.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Defines a web socket expectation. When this expectation block is configured, at least one web socket connection will be expected in order
     * for the verification to pass.
     */
    WebSocketExpectations ws(String path, Consumer<WebSocketExpectations> config);
}
