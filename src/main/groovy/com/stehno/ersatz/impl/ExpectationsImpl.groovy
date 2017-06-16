/*
 * Copyright (C) 2017 Christopher J. Stehno
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
import groovy.transform.CompileStatic
import org.hamcrest.Matcher

import java.util.function.Consumer

import static com.stehno.ersatz.HttpMethod.ANY
import static com.stehno.ersatz.HttpMethod.DELETE
import static com.stehno.ersatz.HttpMethod.GET
import static com.stehno.ersatz.HttpMethod.HEAD
import static com.stehno.ersatz.HttpMethod.OPTIONS
import static com.stehno.ersatz.HttpMethod.PATCH
import static com.stehno.ersatz.HttpMethod.POST
import static com.stehno.ersatz.HttpMethod.PUT
import static org.hamcrest.Matchers.equalTo

/**
 * Implementation of the <code>Expectations</code> interface.
 */
@CompileStatic @SuppressWarnings(['ConfusingMethodName', 'MethodCount'])
class ExpectationsImpl implements Expectations {

    private final List<Request> requests = []
    private final RequestDecoders globalDecoders
    private final ResponseEncoders globalEncoders

    ExpectationsImpl(final RequestDecoders globalDecoders, final ResponseEncoders globalEncoders) {
        this.globalDecoders = globalDecoders
        this.globalEncoders = globalEncoders
    }

    /**
     * Removes all expectation configuration, but does not modify global encoders or decoders.
     */
    void clear(){
        requests.clear()
    }

    @Override
    Request any(final String path) {
        any equalTo(path)
    }

    @Override
    Request any(final Matcher<String> matcher) {
        expect new ErsatzRequestWithContent(ANY, matcher, globalDecoders, globalEncoders)
    }

    @Override
    Request any(final String path, @DelegatesTo(Request) final Closure closure) {
        any equalTo(path), closure
    }

    @Override
    Request any(final Matcher<String> matcher, @DelegatesTo(Request) final Closure closure) {
        expect new ErsatzRequestWithContent(ANY, matcher, globalDecoders, globalEncoders), closure
    }

    @Override
    Request any(final String path, final Consumer<Request> consumer) {
        any equalTo(path), consumer
    }

    @Override
    Request any(final Matcher<String> matcher, final Consumer<Request> consumer) {
        expect new ErsatzRequestWithContent(ANY, matcher, globalDecoders, globalEncoders), consumer
    }

    @Override
    Request get(final String path) {
        get equalTo(path)
    }

    @Override
    Request get(final Matcher<String> matcher) {
        expect new ErsatzRequest(GET, matcher, globalEncoders)
    }

    @Override
    Request get(final String path, @DelegatesTo(Request) final Closure closure) {
        get(equalTo(path), closure)
    }

    @Override
    Request get(final Matcher<String> matcher, @DelegatesTo(Request) final Closure closure) {
        expect new ErsatzRequest(GET, matcher, globalEncoders), closure
    }

    @Override
    Request get(String path, Consumer<Request> config) {
        get(equalTo(path), config)
    }

    @Override
    Request get(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(GET, matcher, globalEncoders), config
    }

    @Override
    Request head(String path) {
        head(equalTo(path))
    }

    @Override
    Request head(String path, @DelegatesTo(Request) Closure closure) {
        head(equalTo(path), closure)
    }

    @Override
    Request head(String path, Consumer<Request> config) {
        head(equalTo(path), config)
    }

    @Override
    Request head(Matcher<String> matcher) {
        expect new ErsatzRequest(HEAD, matcher, globalEncoders, true)
    }

    @Override
    Request head(Matcher<String> matcher, @DelegatesTo(Request) Closure closure) {
        expect new ErsatzRequest(HEAD, matcher, globalEncoders, true), closure
    }

    @Override
    Request head(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(HEAD, matcher, globalEncoders), config
    }

    @Override
    RequestWithContent post(String path) {
        post(equalTo(path))
    }

    @Override
    RequestWithContent post(String path, @DelegatesTo(RequestWithContent) Closure closure) {
        post(equalTo(path), closure)
    }

    @Override
    RequestWithContent post(String path, Consumer<RequestWithContent> config) {
        post(equalTo(path), config)
    }

    @Override
    RequestWithContent post(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    RequestWithContent put(String path) {
        put(equalTo(path))
    }

    @Override
    RequestWithContent put(String path, @DelegatesTo(RequestWithContent) Closure closure) {
        put(equalTo(path), closure)
    }

    @Override
    RequestWithContent put(String path, Consumer<RequestWithContent> config) {
        put(equalTo(path), config)
    }

    @Override
    RequestWithContent put(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    Request delete(String path) {
        delete(equalTo(path))
    }

    @Override
    Request delete(String path, @DelegatesTo(Request) Closure closure) {
        delete(equalTo(path), closure)
    }

    @Override
    Request delete(String path, Consumer<Request> config) {
        delete(equalTo(path), config)
    }

    @Override
    Request delete(Matcher<String> matcher) {
        expect new ErsatzRequest(DELETE, matcher, globalEncoders)
    }

    @Override
    Request delete(Matcher<String> matcher, @DelegatesTo(Request) Closure closure) {
        expect new ErsatzRequest(DELETE, matcher, globalEncoders), closure
    }

    @Override
    Request delete(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(DELETE, matcher, globalEncoders), config
    }

    @Override
    RequestWithContent patch(String path) {
        patch(equalTo(path))
    }

    @Override
    RequestWithContent patch(String path, @DelegatesTo(RequestWithContent) Closure closure) {
        patch(equalTo(path), closure)
    }

    @Override
    RequestWithContent patch(String path, Consumer<RequestWithContent> config) {
        patch(equalTo(path), config)
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders, globalEncoders)) as RequestWithContent
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders, globalEncoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders, globalEncoders), config) as RequestWithContent
    }

    @Override
    Request options(String path) {
        options(equalTo(path))
    }

    @Override
    Request options(String path, @DelegatesTo(Request) Closure closure) {
        options(equalTo(path), closure)
    }

    @Override
    Request options(String path, Consumer<Request> config) {
        options(equalTo(path), config)
    }

    @Override
    Request options(Matcher<String> matcher) {
        expect new ErsatzRequest(OPTIONS, matcher, globalEncoders, true)
    }

    @Override
    Request options(Matcher<String> matcher, @DelegatesTo(Request) Closure closure) {
        expect new ErsatzRequest(OPTIONS, matcher, globalEncoders, true), closure
    }

    @Override
    Request options(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(OPTIONS, matcher, globalEncoders), config
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

    /**
     * Used to verify that all request expectations have been called the expected number of times.
     *
     * @return a value of true if all requests are verified
     */
    boolean verify() {
        requests.each { r ->
            assert ((ErsatzRequest) r).verify(), "Expectations for $r were not met."
        }
        true
    }

    private Request expect(final Request request) {
        requests.add(request)
        request
    }

    private Request expect(final Request request, final Closure closure) {
        closure.setDelegate(request)
        closure.call()

        requests.add(request)
        request
    }

    private Request expect(final Request request, final Consumer<? extends Request> consumer) {
        consumer.accept(request)
        requests.add(request)
        request
    }
}
