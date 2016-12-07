/*
 * Copyright (C) 2016 Christopher J. Stehno
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
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Function

import static com.stehno.ersatz.ErsatzServer.NOT_FOUND_BODY
import static com.stehno.ersatz.Verifiers.exactly

class ErsatzRequestSpec extends Specification {

    private static final String STRING_CONTENT = 'Some content'
    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()
    private final ErsatzRequest request = new ErsatzRequest('TEST', '/testing')
    @AutoCleanup('stop') private final ErsatzServer server = new ErsatzServer()

    def 'method and path'() {
        expect:
        request.method == 'TEST' && request.path == '/testing'
    }

    def 'to string'() {
        expect:
        request.toString() == '{ TEST /testing (query=[:], headers=[:], cookies=[:]): counted=0 }'
    }

    def 'headers'() {
        when:
        request.headers(alpha: 'bravo', charlie: 'delta').header('echo', 'foxtrot')

        then:
        request.getHeader('alpha') == 'bravo'
        request.getHeader('charlie') == 'delta'
        request.getHeader('echo') == 'foxtrot'
        !request.getHeader('nothing')
    }

    def 'header'() {
        expect:
        request.header('something', 'interesting').getHeader('something') == 'interesting'
    }

    def 'queries'() {
        when:
        request.queries(one: ['two'], three: ['four', 'five'])

        then:
        request.getQuery('one') == ['two']
        request.getQuery('three') == ['four', 'five']
        !request.getQuery('notthere')
    }

    def 'query'() {
        when:
        request.query('answer', '42')

        then:
        request.getQuery('answer') == ['42']

        when:
        request.query('answer', '0')

        then:
        request.getQuery('answer') == ['42', '0']
    }

    def 'cookies'() {
        when:
        request.cookies(chocolate: 'yes', amount: 'dozen')

        then:
        request.getCookie('chocolate') == 'yes'
        request.getCookie('amount') == 'dozen'
        !request.getCookie('no-soup')
    }

    def 'cookie'() {
        when:
        request.cookie('vanilla', 'no')

        then:
        request.getCookie('vanilla') == 'no'
    }

    def 'listener (closure)'() {
        setup:
        AtomicInteger counter = new AtomicInteger(0)

        request.listener({ r -> counter.incrementAndGet() })

        when:
        request.mark()
        request.mark()

        then:
        counter.get() == 2
    }

    def 'listener (consumer)'() {
        setup:
        AtomicInteger counter = new AtomicInteger(0)

        request.listener(new Consumer<Request>() {
            @Override void accept(Request r) {
                counter.incrementAndGet()
            }
        })

        when:
        request.mark()
        request.mark()

        then:
        counter.get() == 2
    }

    @Unroll def 'verifier (closure): called #calls expected #expected'() {
        setup:
        request.verifier({ n -> n == expected })

        when:
        calls.times {
            request.mark()
        }

        then:
        request.verify() == verified

        where:
        expected | calls || verified
        2        | 1     || false
        2        | 2     || true
        2        | 3     || false
    }

    @Unroll def 'verifier (consumer): called #calls expected #expected'() {
        setup:
        request.verifier(exactly(expected))

        when:
        calls.times {
            request.mark()
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
        Object body = new Object()

        when:
        request.responds().contentType('something/else').content(body)

        then:
        Response resp = request.currentResponse
        resp.contentType == 'something/else'
        resp.content == body
    }

    def 'responder (closure)'() {
        setup:
        Object contentA = new Object()
        Object contentB = new Object()

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
        request.mark()
        resp = request.currentResponse

        then:
        resp.contentType == 'test/date'
        resp.content == contentB

        when:
        request.mark()
        resp = request.currentResponse

        then:
        resp.contentType == 'test/date'
        resp.content == contentB
    }

    def 'responder (consumer)'() {
        setup:
        Object contentA = new Object()
        Object contentB = new Object()

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
        request.mark()
        resp = request.currentResponse

        then:
        resp.contentType == 'test/date'
        resp.content == contentB

        when:
        request.mark()
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
            get('/test').query('alpha', 'blah').responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test?alpha=blah').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: queries'() {
        setup:
        server.expectations {
            get('/test').queries(alpha: ['one'], bravo: ['two', 'three']).responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test?alpha=one&bravo=two&bravo=three').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').build()).body().string()

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

    def 'condition (closure)'() {
        setup:
        server.expectations {
            get('/test').condition({ r -> (r.headers.get('foo').first as int) < 50 }).responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test').addHeader('foo', '42').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').addHeader('foo', '100').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'condition (function)'() {
        setup:
        Function<ClientRequest, Boolean> fn = new Function<ClientRequest, Boolean>() {
            @Override
            Boolean apply(final ClientRequest ex) {
                (ex.headers.get('foo').first as int) < 50
            }
        }

        server.expectations {
            get('/test').condition(fn).responds().content(STRING_CONTENT)
        }.start()

        when:
        String value = exec(clientGet('/test').addHeader('foo', '42').build()).body().string()

        then:
        value == STRING_CONTENT

        when:
        value = exec(clientGet('/test').addHeader('foo', '100').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    private Builder clientGet(final String path) {
        new Builder().get().url("${server.serverUrl}${path}")
    }

    private okhttp3.Response exec(okhttp3.Request req) {
        client.newCall(req).execute()
    }
}
