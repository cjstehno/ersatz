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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.Expectations
import com.stehno.ersatz.HttpMethod
import com.stehno.ersatz.Request
import com.stehno.ersatz.RequestDecoders
import com.stehno.ersatz.RequestWithContent
import com.stehno.ersatz.ResponseEncoders
import com.stehno.ersatz.WebSocketExpectations
import groovy.transform.CompileStatic
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import space.jasan.support.groovy.closure.ConsumerWithDelegate

import java.util.concurrent.TimeUnit
import java.util.function.Consumer

import static groovy.lang.Closure.DELEGATE_FIRST
import static java.util.concurrent.TimeUnit.SECONDS
import static org.hamcrest.Matchers.equalTo

/**
 * Implementation of the <code>Expectations</code> interface.
 */
@CompileStatic @SuppressWarnings(['ConfusingMethodName', 'MethodCount', 'MethodName'])
class ExpectationsImpl implements Expectations {

    private final List<Request> requests = []
    private final Map<String, WebSocketExpectations> webSockets = [:]
    private final RequestDecoders globalDecoders
    private final ResponseEncoders globalEncoders

    ExpectationsImpl(final RequestDecoders globalDecoders, final ResponseEncoders globalEncoders) {
        this.globalDecoders = globalDecoders
        this.globalEncoders = globalEncoders
    }

    /**
     * Removes all expectation configuration, but does not modify global encoders or decoders.
     */
    void clear() {
        requests.clear()
    }

    @Override
    Request any(final String path) {
        any pathMatcher(path)
    }

    @Override
    Request any(final Matcher<String> matcher) {
        expect new ErsatzRequestWithContent(HttpMethod.ANY, matcher, globalDecoders, globalEncoders)
    }

    @Override
    Request any(final String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) final Closure closure) {
        any pathMatcher(path), closure
    }

    @Override
    Request any(final Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) final Closure closure) {
        expect new ErsatzRequestWithContent(HttpMethod.ANY, matcher, globalDecoders, globalEncoders), closure
    }

    @Override
    Request any(final String path, final Consumer<Request> consumer) {
        any pathMatcher(path), consumer
    }

    @Override
    Request any(final Matcher<String> matcher, final Consumer<Request> consumer) {
        expect new ErsatzRequestWithContent(HttpMethod.ANY, matcher, globalDecoders, globalEncoders), consumer
    }

    @Override
    Request GET(final String path) {
        GET(pathMatcher(path))
    }

    @Override
    Request GET(final Matcher<String> matcher) {
        expect new ErsatzRequest(HttpMethod.GET, matcher, globalEncoders)
    }

    @Override
    Request GET(final String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) final Closure closure) {
        GET(pathMatcher(path), closure)
    }

    @Override
    Request GET(final Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) final Closure closure) {
        expect new ErsatzRequest(HttpMethod.GET, matcher, globalEncoders), closure
    }

    @Override
    Request GET(String path, Consumer<Request> config) {
        GET(pathMatcher(path), config)
    }

    @Override
    Request GET(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(HttpMethod.GET, matcher, globalEncoders), config
    }

    @Override
    Request get(String path) {
        GET(path)
    }

    @Override
    Request get(Matcher<String> matcher) {
        GET(matcher)
    }

    @Override
    Request get(String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        GET(path, closure)
    }

    @Override
    Request get(Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        GET(matcher, closure)
    }

    @Override
    Request get(String path, Consumer<Request> config) {
        GET(path, config)
    }

    @Override
    Request get(Matcher<String> matcher, Consumer<Request> config) {
        GET(matcher, config)
    }

    @Override
    Request HEAD(String path) {
        HEAD(pathMatcher(path))
    }

    @Override
    Request HEAD(String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        HEAD(pathMatcher(path), closure)
    }

    @Override
    Request HEAD(String path, Consumer<Request> config) {
        HEAD(pathMatcher(path), config)
    }

    @Override
    Request HEAD(Matcher<String> matcher) {
        expect new ErsatzRequest(HttpMethod.HEAD, matcher, globalEncoders, true)
    }

    @Override
    Request HEAD(Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        expect new ErsatzRequest(HttpMethod.HEAD, matcher, globalEncoders, true), closure
    }

    @Override
    Request HEAD(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(HttpMethod.HEAD, matcher, globalEncoders), config
    }

    @Override
    Request head(String path) {
        HEAD(path)
    }

    @Override
    Request head(Matcher<String> matcher) {
        HEAD(matcher)
    }

    @Override
    Request head(String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        HEAD(path, closure)
    }

    @Override
    Request head(Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        HEAD(matcher, closure)
    }

    @Override
    Request head(String path, Consumer<Request> config) {
        HEAD(path, config)
    }

    @Override
    Request head(Matcher<String> matcher, Consumer<Request> config) {
        HEAD(matcher, config)
    }

    @Override
    RequestWithContent POST(String path) {
        POST(pathMatcher(path))
    }

    @Override
    RequestWithContent POST(String path, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        POST(pathMatcher(path), closure)
    }

    @Override
    RequestWithContent POST(String path, Consumer<RequestWithContent> config) {
        POST(pathMatcher(path), config)
    }

    @Override
    RequestWithContent POST(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(HttpMethod.POST, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent POST(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        expect(new ErsatzRequestWithContent(HttpMethod.POST, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent POST(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(HttpMethod.POST, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    RequestWithContent post(String path) {
        POST(path)
    }

    @Override
    RequestWithContent post(Matcher<String> matcher) {
        POST(matcher)
    }

    @Override
    RequestWithContent post(String path, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        POST(path, closure)
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        POST(matcher, closure)
    }

    @Override
    RequestWithContent post(String path, Consumer<RequestWithContent> config) {
        POST(path, config)
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        POST(matcher, config)
    }

    @Override
    RequestWithContent PUT(String path) {
        PUT(pathMatcher(path))
    }

    @Override
    RequestWithContent PUT(String path, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        PUT(pathMatcher(path), closure)
    }

    @Override
    RequestWithContent PUT(String path, Consumer<RequestWithContent> config) {
        PUT(pathMatcher(path), config)
    }

    @Override
    RequestWithContent PUT(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(HttpMethod.PUT, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent PUT(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        expect(new ErsatzRequestWithContent(HttpMethod.PUT, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent PUT(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(HttpMethod.PUT, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    RequestWithContent put(String path) {
        PUT(path)
    }

    @Override
    RequestWithContent put(Matcher<String> matcher) {
        PUT(matcher)
    }

    @Override
    RequestWithContent put(String path, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        PUT(path, closure)
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        PUT(matcher, closure)
    }

    @Override
    RequestWithContent put(String path, Consumer<RequestWithContent> config) {
        PUT(path, config)
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        PUT(matcher, config)
    }

    @Override
    Request DELETE(String path) {
        DELETE(pathMatcher(path))
    }

    @Override
    Request DELETE(String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        DELETE(pathMatcher(path), closure)
    }

    @Override
    Request DELETE(String path, Consumer<Request> config) {
        DELETE(pathMatcher(path), config)
    }

    @Override
    Request DELETE(Matcher<String> matcher) {
        expect new ErsatzRequest(HttpMethod.DELETE, matcher, globalEncoders)
    }

    @Override
    Request DELETE(Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        expect new ErsatzRequest(HttpMethod.DELETE, matcher, globalEncoders), closure
    }

    @Override
    Request DELETE(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(HttpMethod.DELETE, matcher, globalEncoders), config
    }

    @Override
    Request delete(String path) {
        DELETE(path)
    }

    @Override
    Request delete(Matcher<String> matcher) {
        DELETE(matcher)
    }

    @Override
    Request delete(String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        DELETE(path, closure)
    }

    @Override
    Request delete(Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        DELETE(matcher, closure)
    }

    @Override
    Request delete(String path, Consumer<Request> config) {
        DELETE(path, config)
    }

    @Override
    Request delete(Matcher<String> matcher, Consumer<Request> config) {
        DELETE(matcher, config)
    }

    @Override
    RequestWithContent PATCH(String path) {
        PATCH(pathMatcher(path))
    }

    @Override
    RequestWithContent PATCH(String path, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        PATCH(pathMatcher(path), closure)
    }

    @Override
    RequestWithContent PATCH(String path, Consumer<RequestWithContent> config) {
        PATCH(pathMatcher(path), config)
    }

    @Override
    RequestWithContent PATCH(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(HttpMethod.PATCH, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent PATCH(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        expect(new ErsatzRequestWithContent(HttpMethod.PATCH, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent PATCH(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(HttpMethod.PATCH, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    RequestWithContent patch(String path) {
        PATCH(path)
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher) {
        PATCH(matcher)
    }

    @Override
    RequestWithContent patch(String path, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        PATCH(path, closure)
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, @DelegatesTo(value = RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        PATCH(matcher, closure)
    }

    @Override
    RequestWithContent patch(String path, Consumer<RequestWithContent> config) {
        PATCH(path, config)
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        PATCH(matcher, config)
    }

    @Override
    Request OPTIONS(String path) {
        OPTIONS(pathMatcher(path))
    }

    @Override
    Request OPTIONS(String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        OPTIONS(pathMatcher(path), closure)
    }

    @Override
    Request OPTIONS(String path, Consumer<Request> config) {
        OPTIONS(pathMatcher(path), config)
    }

    @Override
    Request OPTIONS(Matcher<String> matcher) {
        expect new ErsatzRequest(HttpMethod.OPTIONS, matcher, globalEncoders, true)
    }

    @Override
    Request OPTIONS(Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        expect new ErsatzRequest(HttpMethod.OPTIONS, matcher, globalEncoders, true), closure
    }

    @Override
    Request OPTIONS(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(HttpMethod.OPTIONS, matcher, globalEncoders), config
    }

    @Override
    Request options(String path) {
        OPTIONS(path)
    }

    @Override
    Request options(Matcher<String> matcher) {
        OPTIONS(matcher)
    }

    @Override
    Request options(String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        OPTIONS(path, closure)
    }

    @Override
    Request options(Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) Closure closure) {
        OPTIONS(matcher, closure)
    }

    @Override
    Request options(String path, Consumer<Request> config) {
        OPTIONS(path, config)
    }

    @Override
    Request options(Matcher<String> matcher, Consumer<Request> config) {
        OPTIONS(matcher, config)
    }

    @Override
    WebSocketExpectations ws(final String path) {
        WebSocketExpectationsImpl wse = new WebSocketExpectationsImpl(path)
        webSockets[path] = wse
        wse
    }

    @Override
    WebSocketExpectations ws(final String path, @DelegatesTo(value = WebSocketExpectations, strategy = DELEGATE_FIRST) Closure closure) {
        ws(path, ConsumerWithDelegate.create(closure))
    }

    @Override
    WebSocketExpectations ws(final String path, Consumer<WebSocketExpectations> config) {
        WebSocketExpectationsImpl wse = new WebSocketExpectationsImpl(path)
        config.accept(wse)

        webSockets[path] = wse

        wse
    }

    Set<String> getWebSocketPaths() {
        webSockets.keySet()
    }

    /**
     * Used to find a request matching the given incoming client request. The first match will be returned.
     *
     * @param clientRequest the incoming client request
     * @return the matching request expectation
     */
    Request findMatch(final ClientRequest clientRequest) {
        requests.find { r -> ((ErsatzRequest) r).matches(clientRequest) }
    }

    WebSocketExpectations findWsMatch(final String path) {
        webSockets[path]
    }

    /**
     * Retrieves an immutable list of the stored request expectations.
     *
     * @return the list of request expectations
     */
    List<Request> getRequests() {
        requests.asImmutable()
    }

    /**
     * Used to verify that all request expectations have been called the expected number of times.
     *
     * This method will block until the call count expectations are met or the timeout expires.
     *
     * @return a value of true if all requests are verified
     */
    boolean verify(final long timeout = 1, final TimeUnit unit = SECONDS) {
        requests.each { r ->
            assert ((ErsatzRequest) r).verify(timeout, unit), "Expectations for $r were not met."
        }

        webSockets.each { p, w ->
            assert ((WebSocketExpectationsImpl) w).verify(timeout, unit), "WebSocket expectations for $w were not met."
        }

        true
    }

    private Request expect(final Request request) {
        requests.add(request)
        request
    }

    private Request expect(final Request request, final Closure closure) {
        expect(request, ConsumerWithDelegate.create(closure))
    }

    private Request expect(final Request request, final Consumer<? extends Request> consumer) {
        consumer.accept(request)
        requests.add(request)
        request
    }

    private static Matcher<String> pathMatcher(final String path) {
        path == '*' ? Matchers.any(String) : equalTo(path)
    }
}
