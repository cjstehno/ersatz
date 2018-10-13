/*
 * Copyright (C) 2018 Christopher J. Stehno
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
import com.stehno.ersatz.Request
import com.stehno.ersatz.RequestDecoders
import com.stehno.ersatz.RequestWithContent
import com.stehno.ersatz.ResponseEncoders
import com.stehno.ersatz.WebSocketExpectations
import groovy.transform.CompileStatic
import org.hamcrest.Matcher
import org.hamcrest.Matchers

import java.util.concurrent.TimeUnit
import java.util.function.Consumer

import static com.stehno.ersatz.HttpMethod.ANY
import static com.stehno.ersatz.HttpMethod.DELETE
import static com.stehno.ersatz.HttpMethod.GET
import static com.stehno.ersatz.HttpMethod.HEAD
import static com.stehno.ersatz.HttpMethod.OPTIONS
import static com.stehno.ersatz.HttpMethod.PATCH
import static com.stehno.ersatz.HttpMethod.POST
import static com.stehno.ersatz.HttpMethod.PUT
import static com.stehno.ersatz.impl.Delegator.delegateTo
import static groovy.lang.Closure.DELEGATE_FIRST
import static java.util.concurrent.TimeUnit.SECONDS
import static org.hamcrest.Matchers.equalTo

/**
 * Implementation of the <code>Expectations</code> interface.
 */
@CompileStatic
@SuppressWarnings(['ConfusingMethodName', 'MethodCount'])
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
        expect new ErsatzRequestWithContent(ANY, matcher, globalDecoders, globalEncoders)
    }

    @Override
    Request any(final String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) final Closure closure) {
        any pathMatcher(path), closure
    }

    @Override
    Request any(final Matcher<String> matcher, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) final Closure closure) {
        expect new ErsatzRequestWithContent(ANY, matcher, globalDecoders, globalEncoders), closure
    }

    @Override
    Request any(final String path, final Consumer<Request> consumer) {
        any pathMatcher(path), consumer
    }

    @Override
    Request any(final Matcher<String> matcher, final Consumer<Request> consumer) {
        expect new ErsatzRequestWithContent(ANY, matcher, globalDecoders, globalEncoders), consumer
    }

    @Override
    Request get(final String path) {
        get pathMatcher(path)
    }

    @Override
    Request get(final Matcher<String> matcher) {
        expect new ErsatzRequest(GET, matcher, globalEncoders)
    }

    @Override
    Request get(final String path, @DelegatesTo(value = Request, strategy = DELEGATE_FIRST) final Closure closure) {
        get(pathMatcher(path), closure)
    }

    @Override
    Request get(final Matcher<String> matcher, @DelegatesTo(value=Request, strategy = DELEGATE_FIRST) final Closure closure) {
        expect new ErsatzRequest(GET, matcher, globalEncoders), closure
    }

    @Override
    Request get(String path, Consumer<Request> config) {
        get(pathMatcher(path), config)
    }

    @Override
    Request get(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(GET, matcher, globalEncoders), config
    }

    @Override
    Request head(String path) {
        head(pathMatcher(path))
    }

    @Override
    Request head(String path, @DelegatesTo(value=Request, strategy = DELEGATE_FIRST) Closure closure) {
        head(pathMatcher(path), closure)
    }

    @Override
    Request head(String path, Consumer<Request> config) {
        head(pathMatcher(path), config)
    }

    @Override
    Request head(Matcher<String> matcher) {
        expect new ErsatzRequest(HEAD, matcher, globalEncoders, true)
    }

    @Override
    Request head(Matcher<String> matcher, @DelegatesTo(value=Request, strategy = DELEGATE_FIRST) Closure closure) {
        expect new ErsatzRequest(HEAD, matcher, globalEncoders, true), closure
    }

    @Override
    Request head(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(HEAD, matcher, globalEncoders), config
    }

    @Override
    RequestWithContent post(String path) {
        post(pathMatcher(path))
    }

    @Override
    RequestWithContent post(String path, @DelegatesTo(value=RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        post(pathMatcher(path), closure)
    }

    @Override
    RequestWithContent post(String path, Consumer<RequestWithContent> config) {
        post(pathMatcher(path), config)
    }

    @Override
    RequestWithContent post(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, @DelegatesTo(value=RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    RequestWithContent put(String path) {
        put(pathMatcher(path))
    }

    @Override
    RequestWithContent put(String path, @DelegatesTo(value=RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        put(pathMatcher(path), closure)
    }

    @Override
    RequestWithContent put(String path, Consumer<RequestWithContent> config) {
        put(pathMatcher(path), config)
    }

    @Override
    RequestWithContent put(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, @DelegatesTo(value=RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    Request delete(String path) {
        delete(pathMatcher(path))
    }

    @Override
    Request delete(String path, @DelegatesTo(value=Request, strategy = DELEGATE_FIRST) Closure closure) {
        delete(pathMatcher(path), closure)
    }

    @Override
    Request delete(String path, Consumer<Request> config) {
        delete(pathMatcher(path), config)
    }

    @Override
    Request delete(Matcher<String> matcher) {
        expect new ErsatzRequest(DELETE, matcher, globalEncoders)
    }

    @Override
    Request delete(Matcher<String> matcher, @DelegatesTo(value=Request, strategy = DELEGATE_FIRST) Closure closure) {
        expect new ErsatzRequest(DELETE, matcher, globalEncoders), closure
    }

    @Override
    Request delete(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(DELETE, matcher, globalEncoders), config
    }

    @Override
    RequestWithContent patch(String path) {
        patch(pathMatcher(path))
    }

    @Override
    RequestWithContent patch(String path, @DelegatesTo(value=RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        patch(pathMatcher(path), closure)
    }

    @Override
    RequestWithContent patch(String path, Consumer<RequestWithContent> config) {
        patch(pathMatcher(path), config)
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, @DelegatesTo(value=RequestWithContent, strategy = DELEGATE_FIRST) Closure closure) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    Request options(String path) {
        options(pathMatcher(path))
    }

    @Override
    Request options(String path, @DelegatesTo(value=Request, strategy = DELEGATE_FIRST) Closure closure) {
        options(pathMatcher(path), closure)
    }

    @Override
    Request options(String path, Consumer<Request> config) {
        options(pathMatcher(path), config)
    }

    @Override
    Request options(Matcher<String> matcher) {
        expect new ErsatzRequest(OPTIONS, matcher, globalEncoders, true)
    }

    @Override
    Request options(Matcher<String> matcher, @DelegatesTo(value=Request, strategy = DELEGATE_FIRST) Closure closure) {
        expect new ErsatzRequest(OPTIONS, matcher, globalEncoders, true), closure
    }

    @Override
    Request options(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(OPTIONS, matcher, globalEncoders), config
    }

    @Override
    WebSocketExpectations ws(final String path) {
        WebSocketExpectationsImpl wse = new WebSocketExpectationsImpl(path)
        webSockets[path] = wse
        wse
    }

    @Override
    WebSocketExpectations ws(final String path, @DelegatesTo(value = WebSocketExpectations, strategy = DELEGATE_FIRST) Closure closure) {
        WebSocketExpectationsImpl wse = delegateTo(new WebSocketExpectationsImpl(path), closure)
        webSockets[path] = wse
        wse
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
     * @return a value of true if all requests are verified
     */
    boolean verify(final long timeout = 1, final TimeUnit unit = SECONDS) {
        requests.each { r ->
            assert ((ErsatzRequest) r).verify(), "Expectations for $r were not met."
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
        delegateTo(request, closure)
        requests.add(request)
        request
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
