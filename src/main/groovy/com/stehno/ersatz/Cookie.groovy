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

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.util.function.Consumer

/**
 * Ersatz abstraction of a request or response cookie. See also the <code>CookieMatcher</code>.
 */
@CompileStatic @EqualsAndHashCode @ToString(includeNames = true)
@SuppressWarnings('ConfusingMethodName')
class Cookie {

    String value
    String comment
    String domain
    String path
    int version
    boolean httpOnly
    Integer maxAge
    boolean secure

    /**
     * Used to configure a request cookie with a configuration closure.
     *
     * @param closure the configuration closure
     * @return the configured cookie
     */
    static Cookie cookie(@DelegatesTo(Cookie) final Closure closure) {
        Cookie cookie = new Cookie()
        closure.delegate = cookie
        closure.call()
        cookie
    }

    /**
     * Used to configure a request cookie with a configuration consumer.
     *
     * @param consumer the configuration consumer
     * @return the configured cookie
     */
    static Cookie cookie(final Consumer<Cookie> consumer) {
        Cookie cookie = new Cookie()
        consumer.accept(cookie)
        cookie
    }

    /**
     * Used to configure the value of the cookie as a String.
     *
     * @param value the cookie value
     * @return this cookie
     */
    Cookie value(String value) {
        this.value = value
        this
    }

    /**
     * Used to configure the cookie comment.
     *
     * @param comment the cookie comment
     * @return this cookie
     */
    Cookie comment(final String comment) {
        this.comment = comment
        this
    }

    /**
     * Used to configure the domain of the cookie.
     *
     * @param domain the cookie domain
     * @return this cookie
     */
    Cookie domain(final String domain) {
        this.domain = domain
        this
    }

    /**
     * Used to configure the cookie path.
     *
     * @param path the cookie path
     * @return this cookie
     */
    Cookie path(final String path) {
        this.path = path
        this
    }

    /**
     * Used to configure the cookie API version.
     *
     * @param version the cookie version
     * @return this cookie
     */
    Cookie version(final int version) {
        this.version = version
        this
    }

    /**
     * Used to configure the <code>httpOnly</code> property of the cookie.
     *
     * @param httpOnly the httpOnly property value
     * @return this cookie
     */
    Cookie httpOnly(final boolean httpOnly) {
        this.httpOnly = httpOnly
        this
    }

    /**
     * Used to configure the max age value for the cookie.
     *
     * @param maxAge the max cookie age
     * @return this cookie
     */
    Cookie maxAge(final Integer maxAge) {
        this.maxAge = maxAge
        this
    }

    /**
     * Used to configure whether or not the cookie is marked as secure.
     *
     * @param secure the value
     * @return this cookie
     */
    Cookie secure(final boolean secure) {
        this.secure = secure
        this
    }
}