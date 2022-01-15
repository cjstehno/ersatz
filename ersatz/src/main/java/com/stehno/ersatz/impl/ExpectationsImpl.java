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

import com.stehno.ersatz.cfg.Expectations;
import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.cfg.Request;
import com.stehno.ersatz.cfg.RequestWithContent;
import com.stehno.ersatz.encdec.RequestDecoders;
import com.stehno.ersatz.encdec.ResponseEncoders;
import com.stehno.ersatz.server.ClientRequest;
import org.hamcrest.Matcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableList;

/**
 * Implementation of the <code>Expectations</code> interface.
 */
public class ExpectationsImpl implements Expectations {

    private final List<Request> requests = new LinkedList<>();
    private RequestDecoders globalDecoders;
    private ResponseEncoders globalEncoders;

    /**
     * Creates a new expectations container with the given encoders and decoders.
     *
     * @param encoders the response encoders
     * @param decoders the request decoders
     */
    public ExpectationsImpl(final ResponseEncoders encoders, final RequestDecoders decoders) {
        this.globalEncoders = encoders;
        this.globalDecoders = decoders;
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

    private <R extends Request> R applyExpectation(final R request, final Consumer<R> consumer) {
        if (consumer != null) {
            consumer.accept(request);
        }

        requests.add(request);

        return request;
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

        return true;
    }

    /**
     * Used to verify that all request expectations have been called the expected number of times.
     * <p>
     * This method will block until the call count expectations are met or the timeout expires.
     *
     * @param timeout the amount of time to wait (in seconds)
     * @return a value of true if all requests are verified
     */
    public boolean verify(final long timeout) {
        return verify(timeout, TimeUnit.SECONDS);
    }

    /**
     * Used to verify that all request expectations have been called the expected number of times.
     * This method will block until the call count expectations are met or the timeout (1 second) expires.
     *
     * @return a value of true if all requests are verified
     */
    public boolean verify() {
        return verify(1, TimeUnit.SECONDS);
    }
}
