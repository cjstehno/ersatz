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
package io.github.cjstehno.ersatz.cfg;

import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.match.HeaderMatcher;
import io.github.cjstehno.ersatz.match.QueryParamMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.val;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.match.ErsatzMatchers.stringIterableMatcher;
import static io.github.cjstehno.ersatz.match.HeaderMatcher.headerMatching;
import static io.github.cjstehno.ersatz.match.QueryParamMatcher.queryExists;
import static io.github.cjstehno.ersatz.match.QueryParamMatcher.queryMatching;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Configuration interface for HTTP request expectations.
 */
public interface Request {

    /**
     * Specifies that the request is secure (HTTPS).
     * @return a reference to this request
     */
    default Request secure() {
        return secure(true);
    }

    /**
     * Specifies whether the request is secure (HTTPS vs HTTP) or not.
     * @param enabled true if the request is secure (HTTPS)
     * @return a reference to this request
     */
    Request secure(final boolean enabled);

    /**
     * Specifies a request header to be configured in the expected request. The value specified must match one of the
     * request header values mapped to the expected header name. Multiple header specification may be added to the
     * expectations; each specified header must exist in the request.
     *
     * @param name  the header name
     * @param value the header value
     * @return this request
     */
    default Request header(final String name, final String value){
       return header(headerMatching(name, value));
    }

    /**
     * Specifies a request header matcher to be configured in the expected request. If multiple matchers are defined
     * with this method, each must match successfully for the request to be matched.
     *
     * @param name    the header name
     * @param matcher the header value matcher
     * @return this request
     */
    default Request header(final String name, final Matcher<Iterable<? super String>> matcher){
        return header(headerMatching(name, matcher));
    }

    /**
     * Specifies a request header matcher to be configured in the expected request. If multiple matchers are defined
     * with this method, each must match successfully for the request to be matched.
     *
     * @param headerMatcher the header matcher
     * @return this request
     */
    Request header(final HeaderMatcher headerMatcher);

    /**
     * Specifies request headers as a Map of names to values to be configured in the expected request. The map values may be <code>String</code> or
     * <code>Matcher&lt;Iterable&lt;String&gt;&gt;</code> instances.
     *
     * @param heads the map of headers
     * @return this request
     */
    default Request headers(final Map<String, Object> heads){
        heads.forEach((k, v) -> {
            if (v instanceof Matcher) {
                header(k, (Matcher<Iterable<? super String>>) v);
            } else {
                header(k, v.toString());
            }
        });
        return this;
    }

    /**
     * Used to specify a request query parameter to be configured in the expected request. As per the HTTP spec, the query string parameters may be
     * specified multiple times with different values to denote a parameter with multiple values.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this request
     */
    default Request query(final String name, final String value) {
        return query(queryMatching(name, value));
    }

    /**
     * Used to specify that the request has the given query string, with no care about its value.
     *
     * @param name  the parameter name
     * @return this request
     */
    default Request query(final String name) {
        return query(queryExists(name));
    }

    /**
     * Used to specify that the request must have a query parameter matching the provided matcher.
     *
     * @param queryMatcher the query param matcher
     * @return this request
     */
    Request query(final QueryParamMatcher queryMatcher);

    /**
     * Used to specify a request query parameter to be configured in the expected request. As per the HTTP spec, the query string parameters may be
     * specified multiple times with different values to denote a parameter with multiple values.
     *
     * @param name   the parameter name
     * @param values the list of values
     * @return this request
     */
    default Request query(final String name, final Iterable<? super String> values) {
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
     * @return this request
     */
    default Request query(final String name, final Matcher<Iterable<? super String>> matcher) {
        return query(queryMatching(name, matcher));
    }

    /**
     * Used to specify a map of request query parameters for configuration on the expected request. The map values may be Strings or Matchers.
     *
     * @param map the map of query parameters
     * @return this request
     */
    default Request queries(final Map<String, Object> map) {
        map.forEach((k, v) -> {
            if (v instanceof Matcher) {
                query(k, (Matcher<Iterable<? super String>>) v);
            } else if (v instanceof Collection) {
                query(k, (Collection<? super String>) v);
            } else {
                query(k, v.toString());
            }
        });
        return this;
    }

    /**
     * Specifies a request cookie to be configured with the given name and value.
     *
     * @param name  the cookie name
     * @param value the cookie value
     * @return this request
     */
    Request cookie(final String name, final String value);

    /**
     * Specifies a request cookie to be configured with the given name and matcher.
     *
     * @param name    the cookie name
     * @param matcher the cookie matcher
     * @return this request
     */
    Request cookie(final String name, final Matcher<Cookie> matcher);

    /**
     * Specifies a matcher for matching all cookies. This is useful with the <code>NoCookiesMatcher</code>.
     *
     * @param matcher the matcher to be used
     * @return this request
     */
    Request cookies(final Matcher<Map<String, Cookie>> matcher);

    /**
     * Used to configure a map of cookies on the request. The map values may be Strings or Matchers. All of the configured matchers must be successful
     * in order for the request to be matched.
     *
     * @param cookies the map of cookies
     * @return this request
     */
    Request cookies(final Map<String, Object> cookies);

    /**
     * Specifies a listener which will be called with the active request whenever this request is matched at test-time.
     *
     * @param listener the request call listener
     * @return a reference to this request
     */
    Request listener(final Consumer<ClientRequest> listener);

    /**
     * Allows the specification of a custom call verifier so that the number of times the request is called may be matched.
     *
     * @param callVerifier the verifier to be used
     * @return a reference to this request
     */
    Request called(final Matcher<Integer> callVerifier);

    /**
     * Configures a call count verifier such that the number of calls must be equivalent to the provided count. This is analogous to calling
     * <code>called(equalTo(n))</code>.
     *
     * @param count the expected call count
     * @return a reference to this request
     */
    Request called(final int count);

    /**
     * Configures a matcher for the <code>ClientRequest</code>, which allows ad-hoc matching based on the request. This does <i>not</i> disallow
     * using other matcher methods, but be careful to consider any potential overlaps or unintended exclusions.
     *
     * @param matcher a matcher based on the <code>ClientRequest</code> object
     * @return a reference to this request
     */
    Request matcher(final Matcher<ClientRequest> matcher);

    /**
     * Initiates the definition of a response for this request.
     *
     * @return a response for this request, for configuration
     */
    Response responds();

    /**
     * Allows for configuration of a <code>Response</code> by the given <code>Consumer</code>, which will have a <code>Response</code> object passed
     * into it.
     *
     * @param responder the <code>Consumer&lt;Response&gt;</code> to provide configuration of the response
     * @return a reference to this request
     */
    Request responder(final Consumer<Response> responder);
}
