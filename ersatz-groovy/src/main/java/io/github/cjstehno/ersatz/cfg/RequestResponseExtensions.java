/**
 * Copyright (C) 2024 Christopher J. Stehno
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
import io.github.cjstehno.ersatz.match.PathMatcher;
import org.hamcrest.Matcher;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;

/**
 * Groovy extensions of the RequestResponse class to provide Groovy DSL enhancements.
 */
@SuppressWarnings("checkstyle:MethodName")
public class RequestResponseExtensions {

    /**
     * Allows for configuration of a <code>Response</code> by the given Groovy <code>Closure</code>, which will delegate to a <code>Response</code>
     * instance passed into it for configuration using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param closure the <code>Closure&lt;Response&gt;</code> to provide configuration of the response
     * @return a reference to this request
     */
    public static Request responder(
        final Request self,
        @DelegatesTo(value = Response.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.responder(ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows for configuration of a chunked response using a Groovy <code>Closure</code>, which will delegate to a
     * <code>ChunkingConfig</code> instance passed into it for configuration using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param closure the <code>Closure</code> to provide configuration
     * @return a reference to the configured response
     */
    public static Response chunked(
        final Response self,
        @DelegatesTo(value = ChunkingConfig.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.chunked(ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a request expectation matching any request method using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path.
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request ANY(
        final AnyExpectations self,
        final String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.ANY(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a request expectation matching any request method using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request ANY(
        final AnyExpectations self,
        final Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.ANY(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a request expectation matching any request method using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request ANY(
        final AnyExpectations self,
        final PathMatcher pathMatcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.ANY(pathMatcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path.
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request GET(
        final GetExpectations self,
        final String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.GET(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request GET(
        final GetExpectations self,
        final Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.GET(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a GET request expectation using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request GET(
        final GetExpectations self,
        final PathMatcher pathMatcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.GET(pathMatcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request HEAD(
        final HeadExpectations self,
        final String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.HEAD(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request HEAD(
        final HeadExpectations self,
        final Matcher<String> matcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.HEAD(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a HEAD request expectation using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request HEAD(
        final HeadExpectations self,
        final PathMatcher pathMatcher,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.HEAD(pathMatcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request DELETE(
        final DeleteExpectations self,
        final String path,
        @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return self.DELETE(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request DELETE(
        final DeleteExpectations self,
        final Matcher<String> matcher,
        final @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.DELETE(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a DELETE request expectation using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request DELETE(
        final DeleteExpectations self,
        final PathMatcher pathMatcher,
        final @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.DELETE(pathMatcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request OPTIONS(
        final OptionsExpectations self,
        final String path,
        final @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.OPTIONS(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request OPTIONS(
        final OptionsExpectations self,
        final Matcher<String> matcher,
        final @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.OPTIONS(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a OPTIONS request expectation using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>Request</code> configuration object
     */
    public static Request OPTIONS(
        final OptionsExpectations self,
        final PathMatcher pathMatcher,
        final @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.OPTIONS(pathMatcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PATCH(
        final PatchExpectations self,
        final String path,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PATCH(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PATCH(
        final PatchExpectations self,
        final Matcher<String> matcher,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PATCH(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PATCH request expectation using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PATCH(
        final PatchExpectations self,
        final PathMatcher pathMatcher,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PATCH(pathMatcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent POST(
        final PostExpectations self,
        final String path,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.POST(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent POST(
        final PostExpectations self,
        final Matcher<String> matcher,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.POST(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent POST(
        final PostExpectations self,
        final PathMatcher pathMatcher,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.POST(pathMatcher, ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PUT(
        final PutExpectations self,
        final String path,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PUT(pathMatching(path), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param self    the type of object being extended
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PUT(
        final PutExpectations self,
        final Matcher<String> matcher,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PUT(pathMatching(matcher), ConsumerWithDelegate.create(closure));
    }

    /**
     * Allows configuration of a PUT request expectation using the Groovy DSL.
     *
     * @param self        the type of object being extended
     * @param pathMatcher the path matcher
     * @param closure     the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    public static RequestWithContent PUT(
        final PutExpectations self,
        final PathMatcher pathMatcher,
        final @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return self.PUT(pathMatcher, ConsumerWithDelegate.create(closure));
    }
}
