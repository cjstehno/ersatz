/**
 * Copyright (C) 2022 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.encdec;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * Ersatz abstraction of a request or response cookie. See also the <code>CookieMatcher</code>.
 */
@AllArgsConstructor @Getter @EqualsAndHashCode @ToString
public class Cookie {

    private String value;
    private String comment;
    private String domain;
    private String path;
    private int version;
    private boolean httpOnly;
    private Integer maxAge;
    private boolean secure;

    /**
     * Creates an empty cookie.
     */
    public Cookie() {
        this(null, null, null, null, 0, false, 0, false);
    }

    /**
     * Used to configure a request cookie with a configuration consumer.
     *
     * @param consumer the configuration consumer
     * @return the configured cookie
     */
    public static Cookie cookie(final Consumer<Cookie> consumer) {
        Cookie cookie = new Cookie();
        consumer.accept(cookie);
        return cookie;
    }

    /**
     * Used to configure the value of the cookie as a String.
     *
     * @param value the cookie value
     * @return this cookie
     */
    public Cookie value(String value) {
        this.value = value;
        return this;
    }

    /**
     * Used to configure the cookie comment.
     *
     * @param comment the cookie comment
     * @return this cookie
     */
    public Cookie comment(final String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Used to configure the domain of the cookie.
     *
     * @param domain the cookie domain
     * @return this cookie
     */
    public Cookie domain(final String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Used to configure the cookie path.
     *
     * @param path the cookie path
     * @return this cookie
     */
    public Cookie path(final String path) {
        this.path = path;
        return this;
    }

    /**
     * Used to configure the cookie API version.
     *
     * @param version the cookie version
     * @return this cookie
     */
    public Cookie version(final int version) {
        this.version = version;
        return this;
    }

    /**
     * Used to configure the <code>httpOnly</code> property of the cookie.
     *
     * @param httpOnly the httpOnly property value
     * @return this cookie
     */
    public Cookie httpOnly(final boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    /**
     * Used to configure the max age value for the cookie.
     *
     * @param maxAge the max cookie age
     * @return this cookie
     */
    public Cookie maxAge(final Integer maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Used to configure whether or not the cookie is marked as secure.
     *
     * @param secure the value
     * @return this cookie
     */
    public Cookie secure(final boolean secure) {
        this.secure = secure;
        return this;
    }
}