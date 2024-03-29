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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.Expectations;
import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.cfg.Request;
import io.github.cjstehno.ersatz.cfg.RequestWithContent;
import io.github.cjstehno.ersatz.cfg.WaitFor;
import io.github.cjstehno.ersatz.cfg.WebSocketExpectations;
import io.github.cjstehno.ersatz.encdec.RequestDecoders;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import io.github.cjstehno.ersatz.match.PathMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.ANY;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.HEAD;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.PATCH;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.POST;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.PUT;
import static io.github.cjstehno.ersatz.cfg.WaitFor.ONE_SECOND;
import static java.util.Collections.unmodifiableList;

/**
 * Implementation of the <code>Expectations</code> interface.
 */
@Slf4j @RequiredArgsConstructor
public class ExpectationsImpl implements Expectations {

    private final List<Request> requests = new LinkedList<>();
    private final Map<String, WebSocketExpectations> webSockets = new LinkedHashMap<>();
    private final ResponseEncoders globalEncoders;
    private final RequestDecoders globalDecoders;

    /**
     * Removes all expectation configuration, but does not modify global encoders or decoders.
     */
    public void clear() {
        requests.clear();
    }

    @Override
    public Request request(final HttpMethod method, final PathMatcher pathMatcher, final Consumer<Request> consumer) {
        return applyExpectation(new ErsatzRequestWithContent(method, pathMatcher, globalDecoders, globalEncoders), consumer);
    }

    private RequestWithContent requestWithContent(
        final HttpMethod method, final PathMatcher pathMatcher, final Consumer<RequestWithContent> consumer
    ) {
        return applyExpectation(new ErsatzRequestWithContent(method, pathMatcher, globalDecoders, globalEncoders), consumer);
    }

    @Override
    public Request ANY(final PathMatcher pathMatcher, final Consumer<Request> consumer) {
        return request(ANY, pathMatcher, consumer);
    }

    @Override
    public Request GET(final PathMatcher pathMatcher, final Consumer<Request> consumer) {
        return request(GET, pathMatcher, consumer);
    }

    @Override
    public Request HEAD(final PathMatcher pathMatcher, final Consumer<Request> consumer) {
        return request(HEAD, pathMatcher, consumer);
    }

    @Override
    public RequestWithContent POST(final PathMatcher pathMatcher, final Consumer<RequestWithContent> consumer) {
        return requestWithContent(POST, pathMatcher, consumer);
    }

    @Override
    public RequestWithContent PUT(final PathMatcher pathMatcher, final Consumer<RequestWithContent> config) {
        return requestWithContent(PUT, pathMatcher, config);
    }

    @Override
    public Request DELETE(final PathMatcher pathMatcher, final Consumer<Request> config) {
        return request(HttpMethod.DELETE, pathMatcher, config);
    }

    @Override
    public RequestWithContent PATCH(final PathMatcher pathMatcher, final Consumer<RequestWithContent> config) {
        return requestWithContent(PATCH, pathMatcher, config);
    }

    @Override
    public Request OPTIONS(final PathMatcher pathMatcher, final Consumer<Request> config) {
        return request(HttpMethod.OPTIONS, pathMatcher, config);
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
     * @param waitFor the amount of time the verification should wait before timing out
     * @return a value of true if all requests are verified
     */
    public boolean verify(final WaitFor waitFor) {
        for (final Request r : requests) {
            if (!((ErsatzRequest) r).verify(waitFor)) {
                log.error("Call count mismatch -> {}", r);
                return false;
            }
        }

        for (final var entry : webSockets.entrySet()) {
            if (!(((WebSocketExpectationsImpl) entry.getValue()).verify(waitFor))) {
                log.error("WebSocket expectations for {} were not met.", ((WebSocketExpectationsImpl) entry.getValue()).getPath());
                return false;
            }
        }

        return true;
    }

    /**
     * Used to verify that all request expectations have been called the expected number of times.
     * This method will block until the call count expectations are met or the timeout (1 second) expires.
     *
     * @return a value of true if all requests are verified
     */
    public boolean verify() {
        return verify(ONE_SECOND);
    }

    @Override public WebSocketExpectations webSocket(final String path, final Consumer<WebSocketExpectations> config) {
        final WebSocketExpectationsImpl wse = new WebSocketExpectationsImpl(path);

        if (config != null) {
            config.accept(wse);
        }

        webSockets.put(path, wse);

        return wse;
    }

    /**
     * Retrieves the set of web socket paths configured by the expectations.
     *
     * @return the set of socket paths
     */
    public Set<String> getWebSocketPaths() {
        return webSockets.keySet();
    }

    /**
     * Finds the web socket expectation matching the given path.
     *
     * @param path the path
     * @return the matching expectation
     */
    public WebSocketExpectations findWsMatch(final String path) {
        return webSockets.get(path);
    }
}
