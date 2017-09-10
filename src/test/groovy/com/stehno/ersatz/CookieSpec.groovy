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
package com.stehno.ersatz

import spock.lang.Specification

import java.util.function.Consumer

class CookieSpec extends Specification {

    def 'cookie (closure: methods)'() {
        when:
        Cookie cookie = Cookie.cookie {
            value 'alpha'
            comment 'Something'
            domain 'localhost'
            path '/foo'
            version 1
            httpOnly true
            maxAge 100
            secure true
        }

        then:
        cookie.value == 'alpha'
        cookie.comment == 'Something'
        cookie.domain == 'localhost'
        cookie.path == '/foo'
        cookie.version == 1
        cookie.httpOnly
        cookie.maxAge == 100
        cookie.secure
    }

    def 'cookie (closure: properties)'() {
        when:
        Cookie cookie = Cookie.cookie {
            value = 'alpha'
            comment = 'Something'
            domain = 'localhost'
            path = '/foo'
            version = 1
            httpOnly = true
            maxAge = 100
            secure = true
        }

        then:
        cookie.value == 'alpha'
        cookie.comment == 'Something'
        cookie.domain == 'localhost'
        cookie.path == '/foo'
        cookie.version == 1
        cookie.httpOnly
        cookie.maxAge == 100
        cookie.secure
    }

    def 'cookie (consumer: methods)'() {
        when:
        Cookie cookie = Cookie.cookie(new Consumer<Cookie>() {
            @Override void accept(final Cookie c) {
                c.value 'alpha'
                c.comment 'Something'
                c.domain 'localhost'
                c.path '/foo'
                c.version 1
                c.httpOnly true
                c.maxAge 100
                c.secure true
            }
        })

        then:
        cookie.value == 'alpha'
        cookie.comment == 'Something'
        cookie.domain == 'localhost'
        cookie.path == '/foo'
        cookie.version == 1
        cookie.httpOnly
        cookie.maxAge == 100
        cookie.secure
    }

    def 'cookie (consumer: properties)'() {
        when:
        Cookie cookie = Cookie.cookie(new Consumer<Cookie>() {
            @Override void accept(final Cookie c) {
                c.value = 'alpha'
                c.comment = 'Something'
                c.domain = 'localhost'
                c.path = '/foo'
                c.version = 1
                c.httpOnly = true
                c.maxAge = 100
                c.secure = true
            }
        })

        then:
        cookie.value == 'alpha'
        cookie.comment == 'Something'
        cookie.domain == 'localhost'
        cookie.path == '/foo'
        cookie.version == 1
        cookie.httpOnly
        cookie.maxAge == 100
        cookie.secure
    }

}
