/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.match;

import com.stehno.ersatz.Cookie;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static org.hamcrest.Matchers.equalTo;

/**
 * Hamcrest matcher used to match Ersatz request cookie instances.
 */
public class CookieMatcher extends BaseMatcher<Cookie> {

    private final Map<String, Matcher> matchers = new LinkedHashMap<>();

    /**
     * Configures the cookie matcher with a closure delegating to a <code>CookieMatcher</code> instance.
     *
     * @param closure the configuration closure
     * @return the configured matcher
     */
    public static CookieMatcher cookieMatcher(@DelegatesTo(value = CookieMatcher.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return cookieMatcher(ConsumerWithDelegate.create(closure));
    }

    /**
     * Configures the cookie matcher with a consumer which is passed a <code>CookieMatcher</code> instance to configure.
     *
     * @param consumer the configuration consumer
     * @return the configured matcher
     */
    public static CookieMatcher cookieMatcher(final Consumer<CookieMatcher> consumer) {
        CookieMatcher cookieMatcher = new CookieMatcher();
        consumer.accept(cookieMatcher);
        return cookieMatcher;
    }

    /**
     * Applies a matcher for the specified cookie value. This is equivalent to calling <code>value(Matchers.equalTo('some value'))</code>.
     *
     * @param val the value string
     * @return a reference to the matcher being configured
     */
    public CookieMatcher value(final String val) {
        return value(equalTo(val));
    }

    /**
     * Applies the specified matcher to the cookie value.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    public CookieMatcher value(final Matcher<String> matcher) {
        matchers.put("value", matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie comment value. This is equivalent to calling <code>comment(Matchers.equalTo('some value'))</code>.
     *
     * @param str the comment string
     * @return a reference to the matcher being configured
     */
    public CookieMatcher comment(final String str) {
        return comment(equalTo(str));
    }

    /**
     * Applies the specified matcher to the cookie comment.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    public CookieMatcher comment(final Matcher<String> matcher) {
        matchers.put("comment", matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie domain value. This is equivalent to calling <code>domain(Matchers.equalTo('some value'))</code>.
     *
     * @param str the domain string
     * @return a reference to the matcher being configured
     */
    public CookieMatcher domain(final String str) {
        return domain(equalTo(str));
    }

    /**
     * Applies the specified matcher to the cookie domain.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    public CookieMatcher domain(final Matcher<String> matcher) {
        matchers.put("domain", matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie path value. This is equivalent to calling <code>path(Matchers.equalTo('some value'))</code>.
     *
     * @param str the path string
     * @return a reference to the matcher being configured
     */
    public CookieMatcher path(final String str) {
        return path(equalTo(str));
    }

    /**
     * Applies the specified matcher to the cookie path.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    public CookieMatcher path(final Matcher<String> matcher) {
        matchers.put("path", matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie version value. This is equivalent to calling <code>version(Matchers.equalTo(1))</code>.
     *
     * @param vers the version string
     * @return a reference to the matcher being configured
     */
    public CookieMatcher version(final int vers) {
        return version(equalTo(vers));
    }

    /**
     * Applies the specified matcher to the cookie version.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    public CookieMatcher version(final Matcher<Integer> matcher) {
        matchers.put("version", matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie http-only value.
     *
     * @param httpOnly the httpOnly state
     * @return a reference to the matcher being configured
     */
    public CookieMatcher httpOnly(final boolean httpOnly) {
        matchers.put("httpOnly", equalTo(httpOnly));
        return this;
    }

    /**
     * Applies a matcher for the specified cookie max-age value. This is equivalent to calling <code>maxAge(Matchers.equalTo(age))</code>.
     *
     * @param age the max-age value
     * @return a reference to the matcher being configured
     */
    public CookieMatcher maxAge(final int age) {
        return maxAge(equalTo(age));
    }

    /**
     * Applies the specified matcher to the cookie max-age value.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    public CookieMatcher maxAge(final Matcher<Integer> matcher) {
        matchers.put("maxAge", matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie secure value.
     *
     * @param secure the secure state
     * @return a reference to the matcher being configured
     */
    public CookieMatcher secure(final boolean secure) {
        matchers.put("secure", equalTo(secure));
        return this;
    }

    @Override
    public boolean matches(final Object item) {
        if (!(item instanceof Cookie)) {
            return false;
        }

        final var cookie = (Cookie) item;

        return matchers.entrySet().stream().allMatch(entry -> {
            final var field = entry.getKey();
            final var matcher = entry.getValue();

            switch (field) {
                case "value":
                    return matcher.matches(cookie.getValue());
                case "comment":
                    return matcher.matches(cookie.getComment());
                case "domain":
                    return matcher.matches(cookie.getDomain());
                case "path":
                    return matcher.matches(cookie.getPath());
                case "maxAge":
                    return matcher.matches(cookie.getMaxAge());
                case "httpOnly":
                    return matcher.matches(cookie.isHttpOnly());
                case "secure":
                    return matcher.matches(cookie.isSecure());
                case "version":
                    return matcher.matches(cookie.getVersion());
                default:
                    return false;
            }
        });
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Cookie matching ");

        matchers.forEach((field, matcher) -> {
            description.appendText(field + "(");
            matcher.describeTo(description);
            description.appendText(") ");
        });
    }
}

