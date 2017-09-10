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
