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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.cfg.Request;
import io.github.cjstehno.ersatz.cfg.Response;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import io.github.cjstehno.ersatz.impl.matchers.RequestSchemeMatcher;
import io.github.cjstehno.ersatz.match.HeaderMatcher;
import io.github.cjstehno.ersatz.match.PathMatcher;
import io.github.cjstehno.ersatz.match.QueryParamMatcher;
import io.github.cjstehno.ersatz.match.RequestCookieMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.match.HttpMethodMatcher.methodMatching;
import static io.github.cjstehno.ersatz.util.Timeout.isTrueBefore;
import static java.util.Collections.unmodifiableList;
import static org.hamcrest.Matchers.anything;

/**
 * <code>Request</code> implementation representing requests without body content.
 */
public class ErsatzRequest implements Request {

    private final List<Matcher<ClientRequest>> matchers = new LinkedList<>();
    private final List<Consumer<ClientRequest>> listeners = new LinkedList<>();
    private final List<Response> responses = new LinkedList<>();
    private final ResponseEncoders globalEncoders;
    private final boolean emptyResponse;
    private Matcher<?> callVerifier = anything();
    private final AtomicInteger callCount = new AtomicInteger(0);

    /**
     * Creates a new request with the specified method, path matcher and optional empty response flag (defaults to false).
     *
     * @param meth           the request method
     * @param pathMatcher    the path matcher
     * @param globalEncoders the shared global encoders
     * @param noResponse     whether this is a request with an empty response (defaults to false)
     */
    public ErsatzRequest(final HttpMethod meth, final PathMatcher pathMatcher, final ResponseEncoders globalEncoders, final boolean noResponse) {
        matchers.add(methodMatching(meth));
        matchers.add(pathMatcher);

        this.globalEncoders = globalEncoders;
        this.emptyResponse = noResponse;
    }

    /**
     * Creates a new request with the specified method, path matcher.
     *
     * @param meth           the request method
     * @param pathMatcher    the path matcher
     * @param globalEncoders the shared global encoders
     */
    public ErsatzRequest(final HttpMethod meth, final PathMatcher pathMatcher, final ResponseEncoders globalEncoders) {
        this(meth, pathMatcher, globalEncoders, false);
    }

    @Override
    public Request secure(final boolean value) {
        matchers.add(new RequestSchemeMatcher(value));
        return this;
    }

    @Override public Request header(final HeaderMatcher headerMatcher) {
        matchers.add(headerMatcher);
        return this;
    }

    @Override public Request query(final QueryParamMatcher queryMatcher) {
        matchers.add(queryMatcher);
        return this;
    }

    @Override public Request cookie(RequestCookieMatcher cookieMatcher) {
        matchers.add(cookieMatcher);
        return this;
    }

    @Override
    public Request listener(final Consumer<ClientRequest> listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public Response responds() {
        final Response response = newResponse();
        responses.add(response);
        return response;
    }

    @Override
    public Request responder(final Consumer<Response> responder) {
        final Response response = newResponse();
        responder.accept(response);
        responses.add(response);
        return this;
    }

    @Override
    public Request called(final Matcher<Integer> callVerifier) {
        this.callVerifier = callVerifier;
        return this;
    }

    @Override
    public Request matcher(final Matcher<ClientRequest> matcher) {
        matchers.add(matcher);
        return this;
    }

    /**
     * Used to verify that the request has been called the expected number of times. By default, there is no
     * verification criteria, they must be configured using one of the <code>called()</code> methods.
     * <p>
     * This method will block until the call count condition is met or the timeout is exceeded.
     *
     * @param timeout the timeout duration
     * @param unit    the timeout duration units
     * @return true if the call count matches the expected verification criteria
     */
    public boolean verify(final long timeout, final TimeUnit unit) {
        return isTrueBefore(
            () -> callVerifier.matches(callCount.get()),
            timeout,
            unit
        );
    }

    /**
     * Used to determine whether the incoming client request matches this configured request. All configured matchers must return
     * <code>true</code> in order for the match to be successful. By default, all request have a matcher for request method and request path, the
     * others are optional.
     *
     * @param clientRequest the incoming client request
     * @return true if the incoming request matches the configured request
     */
    public boolean matches(final ClientRequest clientRequest) {
        return matchers.stream().allMatch(m -> m.matches(clientRequest));
    }

    /**
     * Used to retrieve the configured matchers in the request.
     *
     * @return an immutable list of the configured matchers.
     */
    public List<Matcher<ClientRequest>> getRequestMatchers() {
        return unmodifiableList(matchers);
    }

    /**
     * Adds a request matcher to the configured list of matchers.
     *
     * @param matcher the matcher to be added
     */
    protected void addMatcher(final Matcher<ClientRequest> matcher) {
        matchers.add(matcher);
    }

    /**
     * Creates a new response container for this request.
     *
     * @return a new response container
     */
    protected Response newResponse() {
        return new ErsatzResponse(emptyResponse, globalEncoders);
    }

    /**
     * Used to retrieve the current response in the response list (based on the call count). The last response in the list will be sent to all future
     * calls.
     *
     * @return the current response
     */
    public ErsatzResponse getCurrentResponse() {
        final int currentCount = callCount.get();
        final int index = currentCount >= responses.size() ? responses.size() - 1 : currentCount;
        return index >= 0 ? (ErsatzResponse) responses.get(index) : null;
    }

    /**
     * Used to mark the request as having been called. Any configured listeners will be called after the call count has been incremented.
     *
     * @param cr the client request to be marked
     */
    public void mark(final ClientRequest cr) {
        callCount.incrementAndGet();

        listeners.forEach(listener -> listener.accept(cr));
    }

    @Override public String toString() {
        final var str = new StringBuilder();
        str.append("Expectations (").append(getClass().getSimpleName()).append("): ");

        matchers.forEach(m -> {
            final var desc = new StringDescription();
            m.describeTo(desc);
            str.append(desc).append(", ");
        });

        return str.toString();
    }
}
