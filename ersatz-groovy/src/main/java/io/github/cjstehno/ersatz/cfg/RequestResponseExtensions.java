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
package io.github.cjstehno.ersatz.cfg;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.github.cjstehno.ersatz.cfg.*;
import org.hamcrest.Matcher;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static io.github.cjstehno.ersatz.match.ErsatzMatchers.pathMatcher;
import static groovy.lang.Closure.DELEGATE_FIRST;

public class RequestResponseExtensions {

    /**
     * Allows for configuration of a <code>Response</code> by the given Groovy <code>Closure</code>, which will delegate to a <code>Response</code>
     * instance passed into it for configuration using the Groovy DSL.
     *
     * @param closure the <code>Consumer&lt;Response&gt;</code> to provide configuration of the response
     * @return a reference to this request
     */
    public static Request responder(
        final Request self,
        @DelegatesTo(value = Response.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.responder(ConsumerWithDelegate.create(closure));
    }

    public static Response chunked(
        final Response self,
        @DelegatesTo(value = ChunkingConfig.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.chunked(ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a request expectation matching any request method using the Groovy DSL.
     *
     * @param path    the expected request path.
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request ANY(
        final AnyExpectations self,
        String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.ANY(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a request expectation matching any request method using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request ANY(
        final AnyExpectations self,
        Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.ANY(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param path    the expected request path.
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request GET(
        final GetExpectations self,
        String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.GET(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request GET(
        final GetExpectations self,
        Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.GET(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request HEAD(
        final HeadExpectations self,
        String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.HEAD(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request HEAD(
        final HeadExpectations self,
        Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.HEAD(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request DELETE(
        final DeleteExpectations self,
        String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.DELETE(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request DELETE(
        final DeleteExpectations self,
        Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.DELETE(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request OPTIONS(
        final OptionsExpectations self,
        String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.OPTIONS(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request OPTIONS(
        final OptionsExpectations self,
        Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.OPTIONS(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PATCH(
        final PatchExpectations self,
        String path,
        @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PATCH(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PATCH(
        final PatchExpectations self,
        Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PATCH(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent POST(
        final PostExpectations self,
        String path,
        @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.POST(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent POST(
        final PostExpectations self,
        Matcher<String> matcher,
        @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.POST(matcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PUT(
        final PutExpectations self,
        String path,
        @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PUT(pathMatcher(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PUT(
        final PutExpectations self,
        Matcher<String> matcher,
        @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PUT(matcher, ConsumerWithDelegate.create(closure));
    }
}
