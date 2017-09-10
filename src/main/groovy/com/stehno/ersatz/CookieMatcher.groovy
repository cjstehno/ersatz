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
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

import java.util.function.Consumer

import static org.hamcrest.Matchers.equalTo

/**
 * Hamcrest matcher used to match Ersatz request cookie instances.
 */
@CompileStatic
class CookieMatcher extends BaseMatcher<Cookie> {

    private final Map<String, Matcher> matchers = [:]

    /**
     * Configures the cookie matcher with a closure delegating to a <code>CookieMatcher</code> instance.
     *
     * @param closure the configuration closure
     * @return the configured matcher
     */
    static CookieMatcher cookieMatcher(@DelegatesTo(CookieMatcher) final Closure closure) {
        CookieMatcher cookieMatcher = new CookieMatcher()
        closure.delegate = cookieMatcher
        closure.call()
        cookieMatcher
    }

    /**
     * Configures the cookie matcher with a consumer which is passed a <code>CookieMatcher</code> instance to configure.
     *
     * @param closure the configuration consumer
     * @return the configured matcher
     */
    static CookieMatcher cookieMatcher(final Consumer<CookieMatcher> consumer) {
        CookieMatcher cookieMatcher = new CookieMatcher()
        consumer.accept(cookieMatcher)
        cookieMatcher
    }

    /**
     * Applies a matcher for the specified cookie value. This is equivalent to calling <code>value(Matchers.equalTo('some value'))</code>.
     *
     * @param val the value string
     * @return a reference to the matcher being configured
     */
    CookieMatcher value(final String val) {
        value(equalTo(val))
    }

    /**
     * Applies the specified matcher to the cookie value.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    CookieMatcher value(final Matcher<String> matcher) {
        matchers['value'] = matcher
        this
    }

    /**
     * Applies a matcher for the specified cookie comment value. This is equivalent to calling <code>comment(Matchers.equalTo('some value'))</code>.
     *
     * @param str the comment string
     * @return a reference to the matcher being configured
     */
    CookieMatcher comment(final String str) {
        comment equalTo(str)
    }

    /**
     * Applies the specified matcher to the cookie comment.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    CookieMatcher comment(final Matcher<String> matcher) {
        matchers['comment'] = matcher
        this
    }

    /**
     * Applies a matcher for the specified cookie domain value. This is equivalent to calling <code>domain(Matchers.equalTo('some value'))</code>.
     *
     * @param str the domain string
     * @return a reference to the matcher being configured
     */
    CookieMatcher domain(final String str) {
        domain equalTo(str)
    }

    /**
     * Applies the specified matcher to the cookie domain.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    CookieMatcher domain(final Matcher<String> matcher) {
        matchers['domain'] = matcher
        this
    }

    /**
     * Applies a matcher for the specified cookie path value. This is equivalent to calling <code>path(Matchers.equalTo('some value'))</code>.
     *
     * @param str the path string
     * @return a reference to the matcher being configured
     */
    CookieMatcher path(final String str) {
        path equalTo(str)
    }

    /**
     * Applies the specified matcher to the cookie path.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    CookieMatcher path(final Matcher<String> matcher) {
        matchers['path'] = matcher
        this
    }

    /**
     * Applies a matcher for the specified cookie version value. This is equivalent to calling <code>version(Matchers.equalTo(1))</code>.
     *
     * @param str the version string
     * @return a reference to the matcher being configured
     */
    CookieMatcher version(final int vers) {
        version equalTo(vers)
    }

    /**
     * Applies the specified matcher to the cookie version.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    CookieMatcher version(final Matcher<Integer> matcher) {
        matchers['version'] = matcher
        this
    }

    /**
     * Applies a matcher for the specified cookie http-only value.
     *
     * @param str the httpOnly state
     * @return a reference to the matcher being configured
     */
    CookieMatcher httpOnly(final boolean httpOnly) {
        matchers['httpOnly'] = equalTo(httpOnly)
        this
    }

    /**
     * Applies a matcher for the specified cookie max-age value. This is equivalent to calling <code>maxAge(Matchers.equalTo(age))</code>.
     *
     * @param str the max-age value
     * @return a reference to the matcher being configured
     */
    CookieMatcher maxAge(final int age) {
        maxAge equalTo(age)
    }

    /**
     * Applies the specified matcher to the cookie max-age value.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    CookieMatcher maxAge(final Matcher<Integer> matcher) {
        matchers['maxAge'] = matcher
        this
    }

    /**
     * Applies a matcher for the specified cookie secure value.
     *
     * @param str the secure state
     * @return a reference to the matcher being configured
     */
    CookieMatcher secure(final boolean secure) {
        matchers['secure'] = equalTo(secure)
        this
    }

    @Override
    boolean matches(final Object item) {
        if (!(item instanceof Cookie)) {
            return false
        }

        Cookie cookie = item as Cookie

        matchers.every { field, matcher ->
            switch (field) {
                case 'value':
                    return matcher.matches(cookie.value)
                case 'comment':
                    return matcher.matches(cookie.comment)
                case 'domain':
                    return matcher.matches(cookie.domain)
                case 'path':
                    return matcher.matches(cookie.path)
                case 'maxAge':
                    return matcher.matches(cookie.maxAge)
                case 'httpOnly':
                    return matcher.matches(cookie.httpOnly)
                case 'secure':
                    return matcher.matches(cookie.secure)
                case 'version':
                    return matcher.matches(cookie.version)
                default:
                    return false
            }
        }
    }

    @Override
    void describeTo(final Description description) {
        description.appendText('Cookie matching ')

        matchers.each { field, matcher ->
            description.appendText("${field}(")
            matcher.describeTo(description)
            description.appendText(') ')
        }
    }
}

