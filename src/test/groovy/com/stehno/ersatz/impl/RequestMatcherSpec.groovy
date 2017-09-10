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

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.CookieMatcher
import com.stehno.ersatz.Decoders
import com.stehno.ersatz.RequestDecoders
import spock.lang.Specification

import static com.stehno.ersatz.HttpMethod.GET
import static com.stehno.ersatz.HttpMethod.HEAD
import static com.stehno.ersatz.ErsatzMatchers.collectionContains
import static com.stehno.ersatz.impl.RequestMatcher.*
import static org.hamcrest.Matchers.*

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
        header('foo', collectionContains('bar')).matches(cr) == result

        where:
        cr                                           || result
        new MockClientRequest().header('foo', 'bar') || true
        new MockClientRequest().header('one', 'two') || false
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
}
