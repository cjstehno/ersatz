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

import spock.lang.Specification

class ErsatzResponseSpec extends Specification {

    private final ErsatzResponse response = new ErsatzResponse()
    private final Object CONTENT_A = new Object()

    def 'content when empty'(){
        when:
        new ErsatzResponse(true).content(CONTENT_A)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'The response is configured as EMPTY and cannot have content.'
    }

    def 'content'(){
        when:
        response.content(CONTENT_A)

        then:
        response.content == CONTENT_A
    }

    def 'content and content-type'(){
        when:
        response.content(CONTENT_A, 'text/info')

        then:
        response.content == CONTENT_A
        response.contentType == 'text/info'
    }

    def 'content-type'(){
        when:
        response.contentType('text/info')

        then:
        response.contentType == 'text/info'
    }

    def 'headers'(){
        when:
        response.headers(alpha:'something', bravo:'other')

        then:
        response.headers.alpha == 'something'
        response.headers.bravo == 'other'
    }

    def 'header'(){
        when:
        response.header('one', 'two')

        then:
        response.headers.one == 'two'
    }

    def 'cookies'(){
        when:
        response.cookies(alpha:'something', bravo:'other')

        then:
        response.cookies.alpha == 'something'
        response.cookies.bravo == 'other'
    }

    def 'cookie'(){
        when:
        response.cookie('one', 'two')

        then:
        response.cookies.one == 'two'
    }

    def 'code'(){
        when:
        response.code(505)

        then:
        response.code == 505
    }
}
