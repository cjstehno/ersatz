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
package com.stehno.ersatz

import okhttp3.OkHttpClient
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

import static com.stehno.ersatz.Verifiers.atLeast

class ErsatzServerSpec extends Specification {

    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()
    private final ErsatzServer ersatzServer = new ErsatzServer()

    def 'prototype: functional'() {
        setup:
        ersatzServer.expectations({ expectations ->
            expectations.get('/foo').responds().content('This is Ersatz!!')
            expectations.get('/bar').responds().content('This is Bar!!')
        } as Consumer<Expectations>)

        ersatzServer.start()

        when:
        String text = "http://localhost:${ersatzServer.port}/foo".toURL().text

        then:
        text == 'This is Ersatz!!'
    }

    def 'prototype: groovy'() {
        setup:
        final AtomicInteger counter = new AtomicInteger();

        ersatzServer.expectations {
            get('/foo').verifier(atLeast(1)).responder {
                content 'This is Ersatz!!'
            }.responder {
                content 'This is another response'
            }

            get('/bar') {
                verifier { cnt -> cnt >= 2 }
                listener { req -> counter.incrementAndGet() }
                responder {
                    content 'This is Bar!!'
                }
            }

            get('/baz').query('alpha', '42').responds().content('The answer is 42')
        }

        ersatzServer.start()

        when:
        def request = new okhttp3.Request.Builder().url(url('/foo')).build()

        then:
        client.newCall(request).execute().body().string() == 'This is Ersatz!!'

        when:
        request = new okhttp3.Request.Builder().url(url('/foo')).build()

        then:
        client.newCall(request).execute().body().string() == 'This is another response'

        when:
        request = new okhttp3.Request.Builder().url(url("/bar")).build();
        def results = [
            client.newCall(request).execute().body().string(),
            client.newCall(request).execute().body().string()
        ]

        then:
        counter.get() == 2
        results.every { it == 'This is Bar!!' }

        when:
        request = new okhttp3.Request.Builder().url(url('/baz?alpha=42')).build();

        then:
        client.newCall(request).execute().body().string() == 'The answer is 42'

        and:
        ersatzServer.verify()
    }

    def cleanup() {
        ersatzServer.stop()
    }

    private String url(final String path) {
        "http://localhost:${ersatzServer.port}${path}"
    }
}
