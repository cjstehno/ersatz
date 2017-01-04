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

import com.stehno.ersatz.*
import groovy.transform.CompileStatic
import org.hamcrest.Matcher

import java.util.function.Consumer

import static com.stehno.ersatz.impl.ErsatzRequest.*
import static org.hamcrest.Matchers.equalTo

/**
 * Implementation of the <code>Expectations</code> interface.
 */
@CompileStatic @SuppressWarnings(['ConfusingMethodName', 'MethodCount'])
class ExpectationsImpl implements Expectations {

    private final List<Request> requests = []
    private final RequestDecoders globalDecoders = new RequestDecoders()

    ExpectationsImpl(final RequestDecoders globalDecoders) {
        this.globalDecoders = globalDecoders
    }

    @Override
    Request get(final String path) {
        get equalTo(path)
    }

    @Override
    Request get(final Matcher<String> matcher) {
        expect new ErsatzRequest(GET, matcher)
    }

    @Override
    Request get(final String path, @DelegatesTo(Request) final Closure closure) {
        get(equalTo(path), closure)
    }

    @Override
    Request get(final Matcher<String> matcher, @DelegatesTo(Request) final Closure closure) {
        expect new ErsatzRequest(GET, matcher), closure
    }

    @Override
    Request get(String path, Consumer<Request> config) {
        get(equalTo(path), config)
    }

    @Override
    Request get(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(GET, matcher), config
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
        expect new ErsatzRequest(HEAD, matcher, true)
    }

    @Override
    Request head(Matcher<String> matcher, @DelegatesTo(Request) Closure closure) {
        expect new ErsatzRequest(HEAD, matcher, true), closure
    }

    @Override
    Request head(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(HEAD, matcher), config
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
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders)) as RequestWithContent
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent post(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(POST, matcher, globalDecoders), config) as RequestWithContent
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
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders)) as RequestWithContent
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent put(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PUT, matcher, globalDecoders), config) as RequestWithContent
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
        expect new ErsatzRequest(DELETE, matcher)
    }

    @Override
    Request delete(Matcher<String> matcher, @DelegatesTo(Request) Closure closure) {
        expect new ErsatzRequest(DELETE, matcher), closure
    }

    @Override
    Request delete(Matcher<String> matcher, Consumer<Request> config) {
        expect new ErsatzRequest(DELETE, matcher), config
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
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders)) as RequestWithContent
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, @DelegatesTo(RequestWithContent) Closure closure) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders), closure) as RequestWithContent
    }

    @Override
    RequestWithContent patch(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        expect(new ErsatzRequestWithContent(PATCH, matcher, globalDecoders), config) as RequestWithContent
    }

    /**
     * Used to find a request matching the given incoming client request. The first match will be returned.
     *
     * @param clientRequest the incoming client request
     * @return the matching request expectation
     */
    Request findMatch(final ClientRequest clientRequest) {
        // FIXME: maybe dump expectations if no match found
        requests.find { r -> ((ErsatzRequest) r).matches(clientRequest) }
    }

    /**
     * Used to verify that all request expectations have been called the expected number of times.
     *
     * @return a value of true if all requests are verified
     */
    boolean verify() {
        // FIXME: move assert into requests
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
