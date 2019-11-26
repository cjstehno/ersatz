/*
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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.*;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static java.util.Collections.unmodifiableList;
import static org.hamcrest.Matchers.equalTo;

/**
 * Implementation of the <code>Expectations</code> interface.
 */
public class ExpectationsImpl implements Expectations {

    // FIXME: change any to ANY
    // FIXME: move deprecated methods into interface defaults
    // FIXME: find a way to break this up by request type (interface default methods or something)

    private final List<Request> requests = new LinkedList<>();
    private final Map<String, WebSocketExpectations> webSockets = new LinkedHashMap<>();
    private final RequestDecoders globalDecoders;
    private final ResponseEncoders globalEncoders;

    public ExpectationsImpl(final RequestDecoders globalDecoders, final ResponseEncoders globalEncoders) {
        this.globalDecoders = globalDecoders;
        this.globalEncoders = globalEncoders;
    }

    /**
     * Removes all expectation configuration, but does not modify global encoders or decoders.
     */
    public void clear() {
        requests.clear();
    }

    @Override
    public Request any(final String path) {
        return any(pathMatcher(path));
    }

    @Override
    public Request any(final Matcher<String> matcher) {
        return expect(new ErsatzRequestWithContent(HttpMethod.ANY, matcher, globalDecoders, globalEncoders));
    }

    @Override
    public Request any(final String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return any(pathMatcher(path), closure);
    }

    @Override
    public Request any(final Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return expect(new ErsatzRequestWithContent(HttpMethod.ANY, matcher, globalDecoders, globalEncoders), closure);
    }

    @Override
    public Request any(final String path, final Consumer<Request> consumer) {
        return any(pathMatcher(path), consumer);
    }

    @Override
    public Request any(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return expect(new ErsatzRequestWithContent(HttpMethod.ANY, matcher, globalDecoders, globalEncoders), consumer);
    }

    @Override
    public Request GET(final String path) {
        return GET(pathMatcher(path));
    }

    @Override
    public Request GET(final Matcher<String> matcher) {
        return expect(new ErsatzRequest(HttpMethod.GET, matcher, globalEncoders));
    }

    @Override
    public Request GET(final String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return GET(pathMatcher(path), closure);
    }

    @Override
    public Request GET(final Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return expect(new ErsatzRequest(HttpMethod.GET, matcher, globalEncoders), closure);
    }

    @Override
    public Request GET(String path, Consumer<Request> config) {
        return GET(pathMatcher(path), config);
    }

    @Override
    public Request GET(Matcher<String> matcher, Consumer<Request> config) {
        return expect(new ErsatzRequest(HttpMethod.GET, matcher, globalEncoders), config);
    }

    @Override
    public Request get(String path) {
        return GET(path);
    }

    @Override
    public Request get(Matcher<String> matcher) {
        return GET(matcher);
    }

    @Override
    public Request get(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return GET(path, closure);
    }

    @Override
    public Request get(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return GET(matcher, closure);
    }

    @Override
    public Request get(String path, Consumer<Request> config) {
        return GET(path, config);
    }

    @Override
    public Request get(Matcher<String> matcher, Consumer<Request> config) {
        return GET(matcher, config);
    }

    @Override
    public Request HEAD(String path) {
        return HEAD(pathMatcher(path));
    }

    @Override
    public Request HEAD(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return HEAD(pathMatcher(path), closure);
    }

    @Override
    public Request HEAD(String path, Consumer<Request> config) {
        return HEAD(pathMatcher(path), config);
    }

    @Override
    public Request HEAD(Matcher<String> matcher) {
        return expect(new ErsatzRequest(HttpMethod.HEAD, matcher, globalEncoders, true));
    }

    @Override
    public Request HEAD(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return expect(new ErsatzRequest(HttpMethod.HEAD, matcher, globalEncoders, true), closure);
    }

    @Override
    public Request HEAD(Matcher<String> matcher, Consumer<Request> config) {
        return expect(new ErsatzRequest(HttpMethod.HEAD, matcher, globalEncoders), config);
    }

    @Override
    public Request head(String path) {
        return HEAD(path);
    }

    @Override
    public Request head(Matcher<String> matcher) {
        return HEAD(matcher);
    }

    @Override
    public Request head(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return HEAD(path, closure);
    }

    @Override
    public Request head(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return HEAD(matcher, closure);
    }

    @Override
    public Request head(String path, Consumer<Request> config) {
        return HEAD(path, config);
    }

    @Override
    public Request head(Matcher<String> matcher, Consumer<Request> config) {
        return HEAD(matcher, config);
    }

    @Override
    public RequestWithContent POST(String path) {
        return POST(pathMatcher(path));
    }

    @Override
    public RequestWithContent POST(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return POST(pathMatcher(path), closure);
    }

    @Override
    public RequestWithContent POST(String path, Consumer<RequestWithContent> config) {
        return POST(pathMatcher(path), config);
    }

    @Override
    public RequestWithContent POST(Matcher<String> matcher) {
        return (RequestWithContent) expect(new ErsatzRequestWithContent(HttpMethod.POST, matcher, globalDecoders, globalEncoders));
    }

    @Override
    public RequestWithContent POST(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return (RequestWithContent) expect(new ErsatzRequestWithContent(HttpMethod.POST, matcher, globalDecoders, globalEncoders), closure);
    }

    @Override
    public RequestWithContent POST(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        final var request = new ErsatzRequestWithContent(HttpMethod.POST, matcher, globalDecoders, globalEncoders);
        config.accept(request);
        requests.add(request);
        return request;
    }

    @Override
    public RequestWithContent post(String path) {
        return POST(path);
    }

    @Override
    public RequestWithContent post(Matcher<String> matcher) {
        return POST(matcher);
    }

    @Override
    public RequestWithContent post(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return POST(path, closure);
    }

    @Override
    public RequestWithContent post(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return POST(matcher, closure);
    }

    @Override
    public RequestWithContent post(String path, Consumer<RequestWithContent> config) {
        return POST(path, config);
    }

    @Override
    public RequestWithContent post(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        return POST(matcher, config);
    }

    @Override
    public RequestWithContent PUT(String path) {
        return PUT(pathMatcher(path));
    }

    @Override
    public RequestWithContent PUT(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PUT(pathMatcher(path), closure);
    }

    @Override
    public RequestWithContent PUT(String path, Consumer<RequestWithContent> config) {
        return PUT(pathMatcher(path), config);
    }

    @Override
    public RequestWithContent PUT(Matcher<String> matcher) {
        return (RequestWithContent) expect(new ErsatzRequestWithContent(HttpMethod.PUT, matcher, globalDecoders, globalEncoders));
    }

    @Override
    public RequestWithContent PUT(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return (RequestWithContent) expect(new ErsatzRequestWithContent(HttpMethod.PUT, matcher, globalDecoders, globalEncoders), closure);
    }

    @Override
    public RequestWithContent PUT(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        final var request = new ErsatzRequestWithContent(HttpMethod.PUT, matcher, globalDecoders, globalEncoders);
        config.accept(request);
        requests.add(request);
        return request;
    }

    @Override
    public RequestWithContent put(String path) {
        return PUT(path);
    }

    @Override
    public RequestWithContent put(Matcher<String> matcher) {
        return PUT(matcher);
    }

    @Override
    public RequestWithContent put(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PUT(path, closure);
    }

    @Override
    public RequestWithContent put(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PUT(matcher, closure);
    }

    @Override
    public RequestWithContent put(String path, Consumer<RequestWithContent> config) {
        return PUT(path, config);
    }

    @Override
    public RequestWithContent put(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        return PUT(matcher, config);
    }

    @Override
    public Request DELETE(String path) {
        return DELETE(pathMatcher(path));
    }

    @Override
    public Request DELETE(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return DELETE(pathMatcher(path), closure);
    }

    @Override
    public Request DELETE(String path, Consumer<Request> config) {
        return DELETE(pathMatcher(path), config);
    }

    @Override
    public Request DELETE(Matcher<String> matcher) {
        return expect(new ErsatzRequest(HttpMethod.DELETE, matcher, globalEncoders));
    }

    @Override
    public Request DELETE(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return expect(new ErsatzRequest(HttpMethod.DELETE, matcher, globalEncoders), closure);
    }

    @Override
    public Request DELETE(Matcher<String> matcher, Consumer<Request> config) {
        return expect(new ErsatzRequest(HttpMethod.DELETE, matcher, globalEncoders), config);
    }

    @Override
    public Request delete(String path) {
        return DELETE(path);
    }

    @Override
    public Request delete(Matcher<String> matcher) {
        return DELETE(matcher);
    }

    @Override
    public Request delete(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return DELETE(path, closure);
    }

    @Override
    public Request delete(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return DELETE(matcher, closure);
    }

    @Override
    public Request delete(String path, Consumer<Request> config) {
        return DELETE(path, config);
    }

    @Override
    public Request delete(Matcher<String> matcher, Consumer<Request> config) {
        return DELETE(matcher, config);
    }

    @Override
    public RequestWithContent PATCH(String path) {
        return PATCH(pathMatcher(path));
    }

    @Override
    public RequestWithContent PATCH(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PATCH(pathMatcher(path), closure);
    }

    @Override
    public RequestWithContent PATCH(String path, Consumer<RequestWithContent> config) {
        return PATCH(pathMatcher(path), config);
    }

    @Override
    public RequestWithContent PATCH(Matcher<String> matcher) {
        return (RequestWithContent) expect(new ErsatzRequestWithContent(HttpMethod.PATCH, matcher, globalDecoders, globalEncoders));
    }

    @Override
    public RequestWithContent PATCH(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return (RequestWithContent) expect(new ErsatzRequestWithContent(HttpMethod.PATCH, matcher, globalDecoders, globalEncoders), closure);
    }

    @Override
    public RequestWithContent PATCH(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        final var request = new ErsatzRequestWithContent(HttpMethod.PATCH, matcher, globalDecoders, globalEncoders);
        config.accept(request);
        requests.add(request);
        return request;
    }

    @Override
    public RequestWithContent patch(String path) {
        return PATCH(path);
    }

    @Override
    public RequestWithContent patch(Matcher<String> matcher) {
        return PATCH(matcher);
    }

    @Override
    public RequestWithContent patch(String path, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PATCH(path, closure);
    }

    @Override
    public RequestWithContent patch(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return PATCH(matcher, closure);
    }

    @Override
    public RequestWithContent patch(String path, Consumer<RequestWithContent> config) {
        return PATCH(path, config);
    }

    @Override
    public RequestWithContent patch(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        return PATCH(matcher, config);
    }

    @Override
    public Request OPTIONS(String path) {
        return OPTIONS(pathMatcher(path));
    }

    @Override
    public Request OPTIONS(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return OPTIONS(pathMatcher(path), closure);
    }

    @Override
    public Request OPTIONS(String path, Consumer<Request> config) {
        return OPTIONS(pathMatcher(path), config);
    }

    @Override
    public Request OPTIONS(Matcher<String> matcher) {
        return expect(new ErsatzRequest(HttpMethod.OPTIONS, matcher, globalEncoders, true));
    }

    @Override
    public Request OPTIONS(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return expect(new ErsatzRequest(HttpMethod.OPTIONS, matcher, globalEncoders, true), closure);
    }

    @Override
    public Request OPTIONS(Matcher<String> matcher, Consumer<Request> config) {
        return expect(new ErsatzRequest(HttpMethod.OPTIONS, matcher, globalEncoders), config);
    }

    @Override
    public Request options(String path) {
        return OPTIONS(path);
    }

    @Override
    public Request options(Matcher<String> matcher) {
        return OPTIONS(matcher);
    }

    @Override
    public Request options(String path, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return OPTIONS(path, closure);
    }

    @Override
    public Request options(Matcher<String> matcher, @DelegatesTo(value = Request.class, strategy = DELEGATE_FIRST) Closure closure) {
        return OPTIONS(matcher, closure);
    }

    @Override
    public Request options(String path, Consumer<Request> config) {
        return OPTIONS(path, config);
    }

    @Override
    public Request options(Matcher<String> matcher, Consumer<Request> config) {
        return OPTIONS(matcher, config);
    }

    @Override
    public WebSocketExpectations ws(final String path) {
        WebSocketExpectationsImpl wse = new WebSocketExpectationsImpl(path);
        webSockets.put(path, wse);
        return wse;
    }

    @Override
    public WebSocketExpectations ws(final String path, @DelegatesTo(value = WebSocketExpectations.class, strategy = DELEGATE_FIRST) Closure closure) {
        return ws(path, ConsumerWithDelegate.create(closure));
    }

    @Override
    public WebSocketExpectations ws(final String path, Consumer<WebSocketExpectations> config) {
        WebSocketExpectationsImpl wse = new WebSocketExpectationsImpl(path);
        config.accept(wse);

        webSockets.put(path, wse);

        return wse;
    }

    public Set<String> getWebSocketPaths() {
        return webSockets.keySet();
    }

    /**
     * Used to find a request matching the given incoming client request. The first match will be returned.
     *
     * @param clientRequest the incoming client request
     * @return the matching request expectation
     */
    // FIXME: consider converting to Optional<Request> here
    public Request findMatch(final ClientRequest clientRequest) {
        return requests.stream().filter(r -> ((ErsatzRequest) r).matches(clientRequest)).findFirst().orElse(null);
    }

    public WebSocketExpectations findWsMatch(final String path) {
        return webSockets.get(path);
    }

    /**
     * Retrieves an immutable list of the stored request expectations.
     *
     * @return the list of request expectations
     */
    public List<Request> getRequests() {
        return unmodifiableList(requests);
    }

    /**
     * Used to verify that all request expectations have been called the expected number of times.
     * <p>
     * This method will block until the call count expectations are met or the timeout expires.
     *
     * @return a value of true if all requests are verified
     */
    public boolean verify(final long timeout, final TimeUnit unit) {
        for (final Request r : requests) {
            if (!((ErsatzRequest) r).verify(timeout, unit)) {
                // FIXME: something else
                throw new IllegalArgumentException("Expectations for " + r + " were not met.");
            }
        }

        for (final String p : webSockets.keySet()) {
            final var w = webSockets.get(p);
            if (!(((WebSocketExpectationsImpl) w).verify(timeout, unit))) {
                // FIXME: something else
                throw new IllegalArgumentException("WebSocket expectations for " + w + " were not met.");
            }
        }

        return true;
    }

    public boolean verify(final long timeout) {
        return verify(timeout, TimeUnit.SECONDS);
    }

    public boolean verify() {
        return verify(1, TimeUnit.SECONDS);
    }

    private Request expect(final Request request) {
        requests.add(request);
        return request;
    }

    private Request expect(final Request request, final Closure closure) {
        return expect(request, ConsumerWithDelegate.create(closure));
    }

    private Request expect(final Request request, final Consumer<Request> consumer) {
        consumer.accept(request);
        requests.add(request);
        return request;
    }

    private static Matcher<String> pathMatcher(final String path) {
        return path.equals("*") ? Matchers.any(String.class) : equalTo(path);
    }
}
