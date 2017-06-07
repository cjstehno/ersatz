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
import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.InMemoryCookieJar
import com.stehno.ersatz.Response
import com.stehno.ersatz.ResponseEncoders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

import static com.stehno.ersatz.Cookie.cookie
import static com.stehno.ersatz.CookieMatcher.cookieMatcher
import static com.stehno.ersatz.ErsatzServer.NOT_FOUND_BODY
import static com.stehno.ersatz.HttpMethod.POST
import static com.stehno.ersatz.NoCookiesMatcher.noCookies
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.nullValue

class ErsatzRequestSpec extends Specification {

    private static final String STRING_CONTENT = 'Some content'
    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()
    private final ErsatzRequest request = new ErsatzRequest(POST, equalTo('/testing'), new ResponseEncoders())
    @AutoCleanup('stop') private final ErsatzServer server = new ErsatzServer()

    def 'to string'() {
        expect:
        request.toString() == 'Expectations (ErsatzRequest): <POST>, "/testing", '
    }

    def 'method and path'() {
        expect:
        request.matches(clientRequest())
    }

    def 'headers'() {
        when:
        request.headers(alpha: 'bravo', charlie: 'delta').header('echo', 'foxtrot')

        then:
        request.matches(cr) == result

        where:
        cr                                                                                                                         || result
        clientRequest().header('alpha', 'bravo').header('charlie', 'delta').header('echo', 'foxtrot')                              || true
        clientRequest().header('alpha', 'bravo').header('echo', 'foxtrot')                                                         || false
        clientRequest().header('alpha', 'bravo').header('charlie', 'delta').header('echo', 'foxtrot').header('nothing', 'nowhere') || true
        clientRequest().header('alpha', 'bravo').header('charlie', 'not-right').header('echo', 'foxtrot')                          || false
    }

    def 'queries'() {
        when:
        request.queries(one: ['two'], three: ['four', 'five']).query('foo', 'bar')

        then:
        request.matches(cr) == result

        where:
        cr                                                                                     || result
        clientRequest().query('one', 'two').query('three', 'four', 'five').query('foo', 'bar') || true
        clientRequest().query('one', 'two').query('three', 'four', 'five')                     || false
        clientRequest().query('one', 'two').query('three', 'xyz', 'five').query('foo', 'bar')  || false
    }

    def 'cookies'() {
        when:
        request.cookies(chocolate: 'yes', amount: 'dozen').cookie('sugar', 'no')

        then:
        request.matches(cr) == result

        where:
        cr                                                                                                               || result
        clientRequest().cookie('amount', 'dozen').cookie('sugar', 'no')                                                  || false
        clientRequest().cookie('chocolate', 'yes').cookie('amount', 'dozen').cookie('sugar', 'no')                       || true
        clientRequest().cookie('chocolate', 'yes').cookie('amount', 'dozen').cookie('sugar', 'no').cookie('more', 'fun') || true
    }

    def 'ensure matcher cookies'() {
        when:
        request.cookies([
            foo: cookieMatcher {
                value 'one'
            },
            bar: cookieMatcher {
                value equalTo('two')
            }
        ])

        then:
        request.matches(cr) == result

        where:
        cr                                                         || result
        clientRequest().cookie('foo', 'one').cookie('bar', 'two')  || true
        clientRequest().cookie('foo', 'one').cookie('bar', 'blah') || false
    }

    def 'cookie equals matching'() {
        when:
        request.cookie('bar', equalTo(cookie {
            value 'blah-blah'
        }))

        then:
        request.matches(cr) == result

        where:
        cr                                         || result
        clientRequest().cookie('bar', 'boo-boo')   || false
        clientRequest().cookie('bar', 'blah-blah') || true
    }

    def 'match one cookie present and one not'() {
        when:
        request.cookies([
            foo: nullValue(),
            bar: cookieMatcher {
                value equalTo('two')
            }
        ])

        then:
        request.matches(cr) == result

        where:
        cr                                                        || result
        clientRequest().cookie('foo', 'one').cookie('bar', 'two') || false
        clientRequest().cookie('bar', 'two')                      || true
    }

    def 'match with no cookies'() {
        when:
        request.cookies(noCookies())

        then:
        request.matches(cr) == result

        where:
        cr                                       || result
        clientRequest()                          || true
        clientRequest().cookie('alpha', 'bravo') || false
    }

    def 'deep cookie properties'() {
        when:
        request.cookie('foo', cookieMatcher {
            value 'alpha'
            comment 'a comment'
            domain 'blah.com'
            path '/some/path'
            maxAge 12345
            version 1
            httpOnly true
            secure true
        })

        then:
        request.matches(cr) == result

        where:
        cr                                                                                                  || result
        clientRequest().cookie('foo', 'alpha')                                                              || false
        clientRequest().cookie('foo', 'alpha', 'a comment', 'blah.com', '/some/path', 12345, true, true, 1) || true
    }

    def 'listener (closure)'() {
        setup:
        AtomicInteger counter = new AtomicInteger(0)

        request.listener({ r -> counter.incrementAndGet() })

        ClientRequest cr = clientRequest()

        when:
        request.mark(cr)
        request.mark(cr)

        then:
        counter.get() == 2
    }

    def 'listener (consumer)'() {
        setup:
        AtomicInteger counter = new AtomicInteger(0)

        request.listener(new Consumer<ClientRequest>() {
            @Override void accept(ClientRequest r) {
                counter.incrementAndGet()
            }
        })

        ClientRequest cr = clientRequest()

        when:
        request.mark(cr)
        request.mark(cr)

        then:
        counter.get() == 2
    }

    @Unroll 'called #calls expected #expected'() {
        setup:
        request.called(equalTo(expected))

        when:
        calls.times {
            request.mark(clientRequest())
        }

        then:
        request.verify() == verified

        where:
        expected | calls || verified
        2        | 1     || false
        2        | 2     || true
        2        | 3     || false
    }

    def 'responds'() {
        setup:
        String body = 'the-body'

        when:
        request.responds().contentType('something/else').content(body)

        then:
        Response resp = request.currentResponse
        resp.contentType == 'something/else'
        resp.content == body
    }

    def 'responder (closure)'() {
        setup:
        Object contentA = 'body-A'
        Object contentB = 'body-B'

        request.responds().contentType('something/else').content(contentA)
        request.responder {
            contentType 'test/date'
            content contentB
        }

        when:
        Response resp = request.currentResponse

        then:
        resp.contentType == 'something/else'
        resp.content == contentA

        when:
        request.mark(clientRequest())
        resp = request.currentResponse

        then:
        resp.contentType == 'test/date'
        resp.content == contentB

        when:
        request.mark(clientRequest())
        resp = request.currentResponse

        then:
        resp.contentType == 'test/date'
        resp.content == contentB
    }

    def 'responder (consumer)'() {
        setup:
        Object contentA = 'body-A'
        Object contentB = 'body-B'

        request.responds().contentType('something/else').content(contentA)
        request.responder(new Consumer<Response>() {
            @Override
            void accept(final Response response) {
                response.contentType 'test/date'
                response.content contentB
            }
        })

        when:
        Response resp = request.currentResponse

        then:
        resp.contentType == 'something/else'
        resp.content == contentA

        when:
        request.mark(clientRequest())
        resp = request.currentResponse

        then:
        resp.contentType == 'test/date'
        resp.content == contentB

        when:
        request.mark(clientRequest())
        resp = request.currentResponse

        then:
        resp.contentType == 'test/date'
        resp.content == contentB
    }

    def 'matching: not found'() {
        setup:
        server.expectations {
            get('/blah').responds().content(new Object())
        }.start()

        expect:
        exec(clientGet('/test').build()).body().string() == NOT_FOUND_BODY
    }

    def 'matching: header'() {
        setup:
        server.expectations {
            get('/test').header('one', 'blah').responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test').addHeader('one', 'blah').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: headers'() {
        setup:
        server.expectations {
            get('/test').headers(alpha: 'one', bravo: 'two').responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test').addHeader('alpha', 'one').addHeader('bravo', 'two').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').addHeader('alpha', 'one').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: query'() {
        setup:
        server.expectations {
            get('/testing').query('alpha', 'blah').responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/testing?alpha=blah').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/testing').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: queries'() {
        setup:
        server.expectations {
            get('/testing').queries(alpha: ['one'], bravo: ['two', 'three']).responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/testing?alpha=one&bravo=two&bravo=three').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/testing').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: cookie'() {
        setup:
        server.expectations {
            get('/test').cookie('flavor', 'chocolate-chip').responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test').addHeader("Cookie", "flavor=chocolate-chip").build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: cookies'() {
        setup:
        server.expectations {
            get('/test').cookies(flavor: 'chocolate-chip').responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test').addHeader("Cookie", "flavor=chocolate-chip").build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    private Builder clientGet(final String path) {
        new Builder().get().url("${server.httpUrl}${path}")
    }

    private okhttp3.Response exec(Request req) {
        client.newCall(req).execute()
    }

    private static MockClientRequest clientRequest() {
        new MockClientRequest(method: POST, path: '/testing')
    }
}
