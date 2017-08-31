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

import com.stehno.ersatz.ResponseEncoders
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.APPLICATION_JSON
import static com.stehno.ersatz.ContentType.APPLICATION_XML
import static com.stehno.ersatz.ContentType.TEXT_PLAIN

class ErsatzResponseSpec extends Specification {

    private final ErsatzResponse response = new ErsatzResponse(false, new ResponseEncoders())
    private final String CONTENT_A = 'content-A'

    def 'content when empty'() {
        when:
        new ErsatzResponse(true).content(CONTENT_A)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'The response is configured as EMPTY and cannot have content.'
    }

    def 'content'() {
        when:
        response.content(CONTENT_A)

        then:
        response.content == CONTENT_A
    }

    def 'content and content-type'() {
        when:
        response.content(CONTENT_A, 'text/info')

        then:
        response.content == CONTENT_A
        response.contentType == 'text/info'
    }

    def 'content and content-type object'() {
        when:
        response.content(CONTENT_A, APPLICATION_JSON)

        then:
        response.content == CONTENT_A
        response.contentType == APPLICATION_JSON.value
    }

    def 'content-type'() {
        when:
        response.contentType('text/info')

        then:
        response.contentType == 'text/info'
    }

    def 'content-type object'() {
        when:
        response.contentType(APPLICATION_XML)

        then:
        response.contentType == APPLICATION_XML.value
    }

    def 'headers'() {
        when:
        response.headers(alpha: 'something', bravo: 'other', charlie:['one', 'two'])

        then:
        response.headers.alpha[0] == 'something'
        response.headers.bravo[0] == 'other'
        response.headers.charlie == ['one', 'two']
    }

    def 'header'() {
        when:
        response.header('one', 'two')

        then:
        response.headers.one[0] == 'two'
    }

    def 'header (multiple)'() {
        when:
        response.header('one', 'two', 'three')

        then:
        response.headers.one == ['two', 'three']
    }

    def 'cookies'() {
        when:
        response.cookies(alpha: 'something', bravo: 'other')

        then:
        response.cookies.alpha == 'something'
        response.cookies.bravo == 'other'
    }

    def 'cookie'() {
        when:
        response.cookie('one', 'two')

        then:
        response.cookies.one == 'two'
    }

    def 'code'() {
        when:
        response.code(505)

        then:
        response.code == 505
    }

    def 'register encoder (string)'(){
        setup:
        response.content = 'foo'
        response.encoder('text/plain', String, { o-> "${o}-bar"})

        expect:
        response.content == 'foo-bar'
    }

    def 'register encoder (object)'(){
        setup:
        response.content = 'foo'
        response.encoder(TEXT_PLAIN, String, { o-> "${o}-bar"})

        expect:
        response.content == 'foo-bar'
    }

    def 'register encoders'(){
        setup:
        ResponseEncoders encoders = new ResponseEncoders({
            register TEXT_PLAIN, String, { o-> "${o}-baz"}
        })

        response.encoders encoders

        when:
        response.content = 'foo'

        then:
        response.content == 'foo-baz'
    }
}
