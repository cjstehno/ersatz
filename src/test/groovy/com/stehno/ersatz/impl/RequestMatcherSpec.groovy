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

import com.stehno.ersatz.*
import org.hamcrest.Matcher
import spock.lang.Specification

import static com.stehno.ersatz.HttpMethod.GET
import static com.stehno.ersatz.HttpMethod.HEAD
import static com.stehno.ersatz.impl.RequestMatcher.*
import static org.hamcrest.Matchers.*
import static org.hamcrest.core.IsIterableContaining.hasItem

class RequestMatcherSpec extends Specification {

    def 'method'() {
        expect:
        method(equalTo(HEAD)).matches(cr) == result

        where:
        cr                                  || result
        new MockClientRequest(method: HEAD) || true
        new MockClientRequest(method: GET)  || false
    }

    def 'path'() {
        expect:
        path(equalTo('/something')).matches(cr) == result

        where:
        cr                                        || result
        new MockClientRequest(path: '/something') || true
        new MockClientRequest(path: '/some')      || false
    }

    def 'content-type'() {
        expect:
        contentType(startsWith('application/')).matches(cr) == result

        where:
        cr                                                     || result
        new MockClientRequest(contentType: 'application/json') || true
        new MockClientRequest(contentType: 'application/')     || true
        new MockClientRequest()                                || false
    }

    def 'header'() {
        expect:
        header('foo', hasItem('bar')).matches(cr) == result

        where:
        cr                                           || result
        new MockClientRequest().header('foo', 'bar') || true
        new MockClientRequest().header('one', 'two') || false
        new MockClientRequest().header('Foo', 'bar') || true
        new MockClientRequest().header('Foo', 'Bar') || false
        new MockClientRequest()                      || false
    }

    def 'query'() {
        expect:
        query('name', contains('alpha', 'blah')).matches(cr) == result

        where:
        cr                                                     || result
        new MockClientRequest().query('name', 'alpha', 'blah') || true
        new MockClientRequest().query('name', 'alpha')         || false
        new MockClientRequest()                                || false
    }

    def 'param'() {
        expect:
        param('email', contains(containsString('@goomail.com'))).matches(cr) == result

        where:
        cr                                                                                                                 || result
        new MockClientRequest()                                                                                            || false
        new MockClientRequest(bodyParameters: [email: ['foo@goomail.com'] as Deque<String>, spam: ['n'] as Deque<String>]) || true
        new MockClientRequest(bodyParameters: [spam: ['n'] as Deque<String>])                                              || false
    }

    def 'cookie'() {
        expect:
        cookie('id', new CookieMatcher().value(equalTo('asdf89s7g'))).matches(cr) == result

        where:
        cr                                                || result
        new MockClientRequest().cookie('id', 'asdf89s7g') || true
        new MockClientRequest().cookie('id', 'assdfsdf')  || false
        new MockClientRequest()                           || false
    }

    def 'body'() {
        setup:
        RequestDecoders decoders = new RequestDecoders({
            register ContentType.TEXT_PLAIN, Decoders.utf8String
        })

        expect:
        body(new DecoderChain(decoders), ContentType.TEXT_PLAIN.value, equalTo('text content')).matches(cr) == result

        where:
        cr                                                || result
        new MockClientRequest()                           || false
        new MockClientRequest(body: 'text content')       || true
        new MockClientRequest(body: 'text other content') || false
    }

    def 'matcher'() {
        setup:

        expect:
        matcher({ ClientRequest r ->
            r.method == HttpMethod.GET && r.contentLength > 10
        } as Matcher<ClientRequest>).matches(cr) == result

        where:
        cr                                                                || result
        new MockClientRequest()                                           || false
        new MockClientRequest(method: HttpMethod.GET, contentLength: 100) || true
    }
}
