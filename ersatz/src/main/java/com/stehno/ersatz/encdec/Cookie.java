/**
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
package com.stehno.ersatz.encdec;

import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * Ersatz abstraction of a request or response cookie. See also the <code>CookieMatcher</code>.
 */
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
     * Creates a cookie with the specified parameters.
     *
     * @param value the cookie value
     * @param comment the comment for the cookie
     * @param domain the cookie domain
     * @param path the cookie path
     * @param version the cookie version
     * @param httpOnly whether or not the cookie is http-only
     * @param maxAge the max age allowed for the cookie
     * @param secure whether or not the cookie is secure
     */
    public Cookie(String value, String comment, String domain, String path, int version, boolean httpOnly, Integer maxAge, boolean secure) {
        this.value = value;
        this.comment = comment;
        this.domain = domain;
        this.path = path;
        this.version = version;
        this.httpOnly = httpOnly;
        this.maxAge = maxAge;
        this.secure = secure;
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

    /**
     * Use to retrieve the value of the cookie.
     *
     * @return the cookie value
     */
    public String getValue() {
        return value;
    }

    /**
     * Use to retrieve the comment for the cookie.
     *
     * @return the cookie comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Use to retrieve the domain of the cookie.
     *
     * @return the cookie domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Use to retrieve the path of the cookie.
     *
     * @return the cookie path
     */
    public String getPath() {
        return path;
    }

    /**
     * Use to retrieve the version of the cookie format.
     *
     * @return the cookie format version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Use to retrieve whether or not the cookie is http-only.
     *
     * @return true, if the cookie is http-only
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * Use to retrieve the max age allowed for the cookie.
     *
     * @return the cookie max age value
     */
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Use to retrieve whether or not the cookie is secure.
     *
     * @return true, if the cookie is secure
     */
    public boolean isSecure() {
        return secure;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Cookie cookie = (Cookie) o;
        return version == cookie.version &&
            httpOnly == cookie.httpOnly &&
            secure == cookie.secure &&
            Objects.equals(value, cookie.value) &&
            Objects.equals(comment, cookie.comment) &&
            Objects.equals(domain, cookie.domain) &&
            Objects.equals(path, cookie.path) &&
            Objects.equals(maxAge, cookie.maxAge);
    }

    @Override public int hashCode() {
        return Objects.hash(value, comment, domain, path, version, httpOnly, maxAge, secure);
    }

    @Override public String toString() {
        return format(
            "Cookie{value='%s', comment='%s', domain='%s', path='%s', version=%d, httpOnly=%s, maxAge=%d, secure=%s}",
            value, comment, domain, path, version, httpOnly, maxAge, secure
        );
    }
}