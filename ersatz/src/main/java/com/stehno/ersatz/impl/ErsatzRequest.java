/**
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.cfg.Request;
import com.stehno.ersatz.cfg.Response;
import com.stehno.ersatz.encdec.Cookie;
import com.stehno.ersatz.encdec.ResponseEncoders;
import com.stehno.ersatz.match.CookieMatcher;
import com.stehno.ersatz.server.ClientRequest;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.core.IsIterableContaining;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.stehno.ersatz.cfg.HttpMethod.*;
import static com.stehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static java.util.Collections.unmodifiableList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

/**
 * <code>Request</code> implementation representing requests without body content.
 */
public class ErsatzRequest implements Request {

    private final List<RequestMatcher> matchers = new LinkedList<>();
    private final List<Consumer<ClientRequest>> listeners = new LinkedList<>();
    private final List<Response> responses = new LinkedList<>();
    private final ResponseEncoders globalEncoders;
    private final boolean emptyResponse;
    private Matcher<?> callVerifier = anything();
    private final AtomicInteger callCount = new AtomicInteger(0);

    /**
     * Creates a new request with the specified method, path matcher and optional empty response flag (defaults to false).
     *
     * @param meth        the request method
     * @param pathMatcher the path matcher
     * @param globalEncoders the shared global encoders
     * @param noResponse  whether or not this is a request with an empty response (defaults to false)
     */
    public ErsatzRequest(final HttpMethod meth, final Matcher<String> pathMatcher, final ResponseEncoders globalEncoders, final boolean noResponse) {
        matchers.add(RequestMatcher.method(meth == ANY ? isOneOf(GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS, TRACE) : equalTo(meth)));
        matchers.add(RequestMatcher.path(pathMatcher));

        this.globalEncoders = globalEncoders;
        this.emptyResponse = noResponse;
    }

    public ErsatzRequest(final HttpMethod meth, final Matcher<String> pathMatcher, final ResponseEncoders globalEncoders) {
        this(meth, pathMatcher, globalEncoders, false);
    }

    @Override
    public Request protocol(final String proto) {
        matchers.add(RequestMatcher.protocol(equalToIgnoringCase(proto)));
        return this;
    }

    @Override
    public Request headers(final Map<String, Object> heads) {
        heads.forEach((k, v) -> {
            if (v instanceof Matcher) {
                header(k, (Matcher<Iterable<? super String>>) v);
            } else {
                header(k, v.toString());
            }
        });
        return this;
    }

    @Override
    public Request header(final String name, final String value) {
        return header(name, IsIterableContaining.hasItem(value));
    }

    @Override
    public Request header(final String name, final Matcher<Iterable<? super String>> value) {
        matchers.add(RequestMatcher.header(name, value));
        return this;
    }

    @Override
    public Request query(final String name, final String value) {
        return query(name, value != null ? IsIterableContaining.hasItem(value) : IsIterableContaining.hasItem(""));
    }

    @Override
    public Request query(final String name) {
        return query(name, (String) null);
    }


    @Override
    public Request query(final String name, final Iterable<? super String> value) {
        final var queryMatchers = new LinkedList<Matcher<? super String>>();
        value.forEach(v -> queryMatchers.add(equalTo(v)));

        return query(name, stringIterableMatcher(queryMatchers));
    }

    @Override
    public Request query(final String name, final Matcher<Iterable<? super String>> matcher) {
        matchers.add(RequestMatcher.query(name, matcher));
        return this;
    }

    @Override
    public Request queries(final Map<String, Object> map) {
        map.forEach((k, v) -> {
            if (v instanceof Matcher) {
                query(k, (Matcher<Iterable<? super String>>) v);
            } else if (v instanceof Collection) {
                query(k, (Collection<? super String>) v);
            } else {
                query(k, v.toString());
            }
        });
        return this;
    }

    @Override
    public Request cookie(final String name, final String value) {
        return cookie(name, new CookieMatcher().value(value));
    }

    @Override
    public Request cookie(final String name, final Matcher<Cookie> value) {
        matchers.add(RequestMatcher.cookie(name, value));
        return this;
    }

    @Override
    public Request cookies(Matcher<Map<String, Cookie>> matcher) {
        matchers.add(RequestMatcher.cookies(matcher));
        return this;
    }

    @Override
    public Request cookies(Map<String, Object> cookies) {
        cookies.forEach((k, v) -> {
            if (v instanceof Matcher) {
                cookie(k, (Matcher<Cookie>) v);
            } else {
                cookie(k, v.toString());
            }
        });
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
    public Request called(final int count) {
        return called(equalTo(count));
    }

    @Override
    public Request matcher(final Matcher<ClientRequest> matcher) {
        matchers.add(RequestMatcher.matcher(matcher));
        return this;
    }

    /**
     * Used to verify that the request has been called the expected number of times. By default there is no verification criteria, they must be
     * configured using one of the <code>called()</code> methods.
     * <p>
     * This method will block until the call count condition is met or the timeout is exceeded.
     *
     * @param timeout the timeout duration
     * @param unit the timeout duration units
     * @return true if the call count matches the expected verification criteria
     */
    public boolean verify(final long timeout, final TimeUnit unit) {
        try {
            await().atMost(timeout, unit).until(new CallCountChecker(callVerifier, callCount));
            return true;
        } catch (ConditionTimeoutException cte) {
            return false;
        }
    }

    /**
     * Used to determine whether or not the incoming client request matches this configured request. All configured matchers must return
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
    public List<RequestMatcher> getRequestMatchers() {
        return unmodifiableList(matchers);
    }

    protected void addMatcher(final RequestMatcher matcher) {
        matchers.add(matcher);
    }

    protected Response newResponse() {
        return new ErsatzResponse(emptyResponse, globalEncoders);
    }

    protected final boolean isEmptyResponse() {
        return emptyResponse;
    }

    protected final ResponseEncoders getGlobalEncoders() {
        return globalEncoders;
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
            m.getMatcher().describeTo(desc);
            str.append(desc).append(", ");
        });

        return str.toString();
    }

    private static class CallCountChecker implements Callable<Boolean> {

        private final Matcher<?> callVerifier;
        private final AtomicInteger callCount;

        CallCountChecker(final Matcher<?> callVerifier, final AtomicInteger callCount) {
            this.callVerifier = callVerifier;
            this.callCount = callCount;
        }

        public Matcher<?> getCallVerifier() {
            return callVerifier;
        }

        public AtomicInteger getCallCount() {
            return callCount;
        }

        @Override
        public Boolean call() throws Exception {
            return callVerifier.matches(callCount.get());
        }
    }
}
