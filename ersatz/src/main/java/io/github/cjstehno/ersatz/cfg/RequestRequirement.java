/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.cfg;

import static io.github.cjstehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static io.github.cjstehno.ersatz.match.HeaderMatcher.headerMatching;
import static io.github.cjstehno.ersatz.match.QueryParamMatcher.queryExists;
import static io.github.cjstehno.ersatz.match.QueryParamMatcher.queryMatching;
import static io.github.cjstehno.ersatz.match.RequestCookieMatcher.cookieMatching;
import static org.hamcrest.CoreMatchers.equalTo;

import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.match.HeaderMatcher;
import io.github.cjstehno.ersatz.match.QueryParamMatcher;
import io.github.cjstehno.ersatz.match.RequestCookieMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import java.util.LinkedList;
import lombok.val;
import org.hamcrest.Matcher;

/**
 * Defines a global request requirement.
 */
public interface RequestRequirement {

    /**
     * Specifies that the request is secure (HTTPS).
     *
     * @return a reference to this request requirement
     */
    default RequestRequirement secure() {
        return secure(true);
    }

    /**
     * Specifies whether the request is secure (HTTPS vs HTTP) or not.
     *
     * @param enabled true if the request is secure (HTTPS)
     * @return a reference to this request requirement
     */
    RequestRequirement secure(final boolean enabled);

    /**
     * Specifies a request header to be configured in the expected request. The value specified must match one of the
     * request header values mapped to the expected header name. Multiple header specification may be added to the
     * expectations; each specified header must exist in the request.
     *
     * @param name  the header name
     * @param value the header value
     * @return this request requirement
     */
    default RequestRequirement header(final String name, final String value) {
        return header(headerMatching(name, value));
    }

    /**
     * Specifies a request header matcher to be configured in the expected request. If multiple matchers are defined
     * with this method, each must match successfully for the request to be matched.
     *
     * @param name    the header name
     * @param matcher the header value matcher
     * @return this request requirement
     */
    default RequestRequirement header(final String name, final Matcher<Iterable<? super String>> matcher) {
        return header(headerMatching(name, matcher));
    }

    /**
     * Specifies a request header matcher to be configured in the expected request. If multiple matchers are defined
     * with this method, each must match successfully for the request to be matched.
     *
     * @param headerMatcher the header matcher
     * @return this request requirement
     */
    RequestRequirement header(final HeaderMatcher headerMatcher);

    /**
     * Used to specify a request query parameter to be configured in the expected request. As per the HTTP spec, the query string parameters may be
     * specified multiple times with different values to denote a parameter with multiple values.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this request requirement
     */
    default RequestRequirement query(final String name, final String value) {
        return query(queryMatching(name, value));
    }

    /**
     * Used to specify that the request has the given query string, with no care about its value.
     *
     * @param name the parameter name
     * @return this request requirement
     */
    default RequestRequirement query(final String name) {
        return query(queryExists(name));
    }

    /**
     * Used to specify that the request must have a query parameter matching the provided matcher.
     *
     * @param queryMatcher the query param matcher
     * @return this request requirement
     */
    RequestRequirement query(final QueryParamMatcher queryMatcher);

    /**
     * Used to specify a request query parameter to be configured in the expected request. As per the HTTP spec, the query string parameters may be
     * specified multiple times with different values to denote a parameter with multiple values.
     *
     * @param name   the parameter name
     * @param values the list of values
     * @return this request requirement
     */
    default RequestRequirement query(final String name, final Iterable<? super String> values) {
        val queryMatchers = new LinkedList<Matcher<? super String>>();
        values.forEach(v -> queryMatchers.add(equalTo(v)));

        return query(queryMatching(name, stringIterableMatcher(queryMatchers)));
    }

    /**
     * Used to specify a request query parameter to be configured in the expected request. As per the HTTP spec, the query string parameters may be
     * specified multiple times with different values to denote a parameter with multiple values.
     *
     * @param name    the parameter name
     * @param matcher the query string matcher
     * @return this request requirement
     */
    default RequestRequirement query(final String name, final Matcher<Iterable<? super String>> matcher) {
        return query(queryMatching(name, matcher));
    }

    /**
     * Specifies a request cookie to be configured with the given name and value.
     *
     * @param name  the cookie name
     * @param value the cookie value
     * @return this request requirement
     */
    default RequestRequirement cookie(final String name, final String value) {
        return cookie(cookieMatching(name, value));
    }

    /**
     * Specifies a request cookie to be configured with the given name and matcher.
     *
     * @param name    the cookie name
     * @param matcher the cookie matcher
     * @return this request requirement
     */
    default RequestRequirement cookie(final String name, final Matcher<Cookie> matcher) {
        return cookie(cookieMatching(name, matcher));
    }

    /**
     * Specifies a request cookie to be configured with the given matcher.
     *
     * @param cookieMatcher the request cookie matcher
     * @return this request requirement
     */
    RequestRequirement cookie(final RequestCookieMatcher cookieMatcher);

    /**
     * Configures a matcher for the <code>ClientRequest</code>, which allows ad-hoc matching based on the request. This does <i>not</i> disallow
     * using other matcher methods, but be careful to consider any potential overlaps or unintended exclusions.
     *
     * @param matcher a matcher based on the <code>ClientRequest</code> object
     * @return a reference to this request requirement
     */
    RequestRequirement matcher(final Matcher<ClientRequest> matcher);
}
