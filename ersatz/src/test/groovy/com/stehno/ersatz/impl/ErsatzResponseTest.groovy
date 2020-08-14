/*
 * Copyright (C) 2020 Christopher J. Stehno
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

import com.stehno.ersatz.encdec.ResponseEncoders
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import static com.stehno.ersatz.cfg.ContentType.*
import static org.junit.jupiter.api.Assertions.*

class ErsatzResponseTest {

    private static final String CONTENT_A = 'content-A'
    private ErsatzResponse response

    @BeforeEach void beforeEach() {
        response = new ErsatzResponse(false, new ResponseEncoders())
    }

    @Test @DisplayName('content when empty')
    void contentWhenEmpty() {
        def thrown = assertThrows(IllegalArgumentException, {
            new ErsatzResponse(true).body(CONTENT_A)
        })
        assertEquals 'The response is configured as EMPTY and cannot have content.', thrown.message
    }

    @Test @DisplayName('content')
    void content() {
        response.body(CONTENT_A)

        assertArrayEquals CONTENT_A.bytes, response.content
    }

    @Test @DisplayName('content and content-type')
    void contentAndContentType() {
        response.body(CONTENT_A, 'text/info')

        assertArrayEquals CONTENT_A.bytes, response.content
        assertEquals 'text/info', response.contentType
    }

    @Test @DisplayName('content and content-type object')
    void contentAndContentTypeObject() {
        response.body(CONTENT_A, APPLICATION_JSON)

        assertArrayEquals CONTENT_A.bytes, response.content
        assertEquals APPLICATION_JSON.value, response.contentType
    }

    @Test @DisplayName('content-type')
    void contentType() {
        response.contentType('text/info')

        assertEquals 'text/info', response.contentType
    }

    @Test @DisplayName('content-type object')
    void contentTypeObject() {
        response.contentType(APPLICATION_XML)

        assertEquals APPLICATION_XML.value, response.contentType
    }

    @Test @DisplayName('headers')
    void headers() {
        response.headers(alpha: 'something', bravo: 'other', charlie: ['one', 'two'])

        assertEquals 'something', response.headers.alpha[0]
        assertEquals 'other', response.headers.bravo[0]
        assertEquals(['one', 'two'], response.headers.charlie)
    }

    @Test @DisplayName('header')
    void header() {
        response.header('one', 'two')

        assertEquals 'two', response.headers.one[0]
    }

    @Test @DisplayName('header (multiple)')
    void headerMultiple() {
        response.header('one', 'two', 'three')

        assertEquals(['two', 'three'], response.headers.one)
    }

    @Test @DisplayName('cookies')
    void cookies() {
        response.cookies(alpha: 'something', bravo: 'other')

        assertEquals 'something', response.cookies.alpha
        assertEquals 'other', response.cookies.bravo
    }

    @Test @DisplayName('cookie')
    void cookie() {
        response.cookie('one', 'two')

        assertEquals 'two', response.cookies.one
    }

    @Test @DisplayName('code')
    void code() {
        response.code(505)

        assertEquals 505, response.code
    }

    @Test @DisplayName('register encoder (string)')
    void registerEncoderString() {
        response.content = 'foo'
        response.encoder('text/plain', String, { o -> "${o}-bar".bytes })

        assertArrayEquals 'foo-bar'.bytes, response.content
    }

    @Test @DisplayName('register encoder (object)')
    void registerEncoderObject() {
        response.content = 'foo'
        response.encoder(TEXT_PLAIN, String, { o -> "${o}-bar".bytes })

        assertArrayEquals 'foo-bar'.bytes, response.content
    }

    @Test @DisplayName('register encoders')
    void registerEncoders() {
        response.encoders new ResponseEncoders({
            register TEXT_PLAIN, String, { o -> "${o}-baz".bytes }
        })

        response.content = 'foo'

        assertArrayEquals 'foo-baz'.bytes, response.content
    }
}
