/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.encdec.Cookie;
import lombok.val;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.equalTo;

/**
 * Hamcrest matcher used to match Ersatz request cookie instances.
 */
public class CookieMatcher extends BaseMatcher<Cookie> {

    private final Map<CookieField, Matcher> matchers = new EnumMap<>(CookieField.class);

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
        matchers.put(CookieField.VALUE, matcher);
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
        matchers.put(CookieField.COMMENT, matcher);
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
        matchers.put(CookieField.DOMAIN, matcher);
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
        matchers.put(CookieField.PATH, matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie version value. This is equivalent to calling <code>version(Matchers.equalTo(1))</code>.
     *
     * @param version the version string
     * @return a reference to the matcher being configured
     */
    public CookieMatcher version(final int version) {
        return version(equalTo(version));
    }

    /**
     * Applies the specified matcher to the cookie version.
     *
     * @param matcher the matcher to be used
     * @return a reference to the matcher being configured
     */
    public CookieMatcher version(final Matcher<Integer> matcher) {
        matchers.put(CookieField.VERSION, matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie http-only value.
     *
     * @param httpOnly the httpOnly state
     * @return a reference to the matcher being configured
     */
    public CookieMatcher httpOnly(final boolean httpOnly) {
        matchers.put(CookieField.HTTP_ONLY, equalTo(httpOnly));
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
        matchers.put(CookieField.MAX_AGE, matcher);
        return this;
    }

    /**
     * Applies a matcher for the specified cookie secure value.
     *
     * @param secure the secure state
     * @return a reference to the matcher being configured
     */
    public CookieMatcher secure(final boolean secure) {
        matchers.put(CookieField.SECURE, equalTo(secure));
        return this;
    }

    @Override
    public boolean matches(final Object item) {
        if (!(item instanceof Cookie)) {
            return false;
        }

        val cookie = (Cookie) item;

        return matchers.entrySet().stream().allMatch(entry -> {
            val matcher = entry.getValue();

            return switch (entry.getKey()) {
                case VALUE -> matcher.matches(cookie.getValue());
                case COMMENT -> matcher.matches(cookie.getComment());
                case DOMAIN -> matcher.matches(cookie.getDomain());
                case PATH -> matcher.matches(cookie.getPath());
                case MAX_AGE -> matcher.matches(cookie.getMaxAge());
                case HTTP_ONLY -> matcher.matches(cookie.isHttpOnly());
                case SECURE -> matcher.matches(cookie.isSecure());
                case VERSION -> matcher.matches(cookie.getVersion());
            };
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

    private enum CookieField {
        VALUE, COMMENT, DOMAIN, PATH, MAX_AGE, HTTP_ONLY, SECURE, VERSION
    }
}

