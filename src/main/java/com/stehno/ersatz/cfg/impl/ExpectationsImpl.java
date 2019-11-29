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
package com.stehno.ersatz.cfg.impl;

import com.stehno.ersatz.ClientRequest;
import com.stehno.ersatz.cfg.*;
import com.stehno.ersatz.encdec.RequestDecoders;
import com.stehno.ersatz.encdec.ResponseEncoders;
import org.hamcrest.Matcher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableList;

/**
 * Implementation of the <code>Expectations</code> interface.
 */
public class ExpectationsImpl implements Expectations {

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
    public Request ANY(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return applyExpectation(new ErsatzRequestWithContent(HttpMethod.ANY, matcher, globalDecoders, globalEncoders), consumer);
    }

    @Override
    public Request GET(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return applyExpectation(new ErsatzRequest(HttpMethod.GET, matcher, globalEncoders), consumer);
    }

    @Override
    public Request HEAD(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return applyExpectation(new ErsatzRequest(HttpMethod.HEAD, matcher, globalEncoders, true), consumer);
    }

    @Override
    public RequestWithContent POST(Matcher<String> matcher, Consumer<RequestWithContent> consumer) {
        return applyExpectation(new ErsatzRequestWithContent(HttpMethod.POST, matcher, globalDecoders, globalEncoders), consumer);
    }

    @Override
    public RequestWithContent PUT(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        return applyExpectation(new ErsatzRequestWithContent(HttpMethod.PUT, matcher, globalDecoders, globalEncoders), config);
    }

    @Override
    public Request DELETE(Matcher<String> matcher, Consumer<Request> config) {
        return applyExpectation(new ErsatzRequest(HttpMethod.DELETE, matcher, globalEncoders), config);
    }

    @Override
    public RequestWithContent PATCH(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        return applyExpectation(new ErsatzRequestWithContent(HttpMethod.PATCH, matcher, globalDecoders, globalEncoders), config);
    }

    @Override
    public Request OPTIONS(Matcher<String> matcher, Consumer<Request> config) {
        return applyExpectation(new ErsatzRequest(HttpMethod.OPTIONS, matcher, globalEncoders), config);
    }

    @Override
    public WebSocketExpectations ws(final String path, Consumer<WebSocketExpectations> config) {
        final WebSocketExpectationsImpl wse = new WebSocketExpectationsImpl(path);

        if (config != null) {
            config.accept(wse);
        }

        webSockets.put(path, wse);

        return wse;
    }

    private <R extends Request> R applyExpectation(final R request, final Consumer<R> consumer) {
        if (consumer != null) {
            consumer.accept(request);
        }

        requests.add(request);

        return request;
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
    public Optional<Request> findMatch(final ClientRequest clientRequest) {
        return requests.stream().filter(r -> ((ErsatzRequest) r).matches(clientRequest)).findFirst();
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
     * @param timeout the amount of time to wait in the specified units
     * @param unit the timeout unit to be used
     * @return a value of true if all requests are verified
     */
    public boolean verify(final long timeout, final TimeUnit unit) {
        for (final Request r : requests) {
            if (!((ErsatzRequest) r).verify(timeout, unit)) {
                throw new IllegalArgumentException("Expectations for " + r + " were not met.");
            }
        }

        for (final String p : webSockets.keySet()) {
            final var w = webSockets.get(p);
            if (!(((WebSocketExpectationsImpl) w).verify(timeout, unit))) {
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
}
