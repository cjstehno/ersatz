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
package io.github.cjstehno.ersatz.impl;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.ANY;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.DELETE;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.HEAD;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.OPTIONS;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.PATCH;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.POST;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.PUT;
import static io.github.cjstehno.ersatz.match.HttpMethodMatcher.methodMatching;
import static org.hamcrest.Matchers.allOf;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.cfg.ProxyServerExpectations;
import io.github.cjstehno.ersatz.impl.matchers.MatchCountingMatcher;
import io.github.cjstehno.ersatz.match.PathMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import org.hamcrest.Matcher;

/**
 * The implementation of the <code>ProxyServerExpectations</code> class.
 */
public class ProxyServerExpectationsImpl implements ProxyServerExpectations {

    @Getter private final List<Matcher<ClientRequest>> matchers = new LinkedList<>();

    @Override public ProxyServerExpectations any(final PathMatcher matcher) {
        return addMatcherFor(ANY, matcher);
    }

    @Override public ProxyServerExpectations get(final PathMatcher matcher) {
        return addMatcherFor(GET, matcher);
    }

    @Override public ProxyServerExpectations head(final PathMatcher matcher) {
        return addMatcherFor(HEAD, matcher);
    }

    @Override public ProxyServerExpectations put(final PathMatcher matcher) {
        return addMatcherFor(PUT, matcher);
    }

    @Override public ProxyServerExpectations post(final PathMatcher matcher) {
        return addMatcherFor(POST, matcher);
    }

    @Override public ProxyServerExpectations delete(final PathMatcher matcher) {
        return addMatcherFor(DELETE, matcher);
    }

    @Override public ProxyServerExpectations patch(final PathMatcher matcher) {
        return addMatcherFor(PATCH, matcher);
    }

    @Override public ProxyServerExpectations options(final PathMatcher matcher) {
        return addMatcherFor(OPTIONS, matcher);
    }

    /**
     * Determines whether the given client request is matched by the expectations.
     *
     * @param clientRequest the client request
     * @return whether the request is matched
     */
    public boolean matches(final ClientRequest clientRequest) {
        return matchers.stream().anyMatch(m -> m.matches(clientRequest));
    }

    private ProxyServerExpectations addMatcherFor(final HttpMethod method, final PathMatcher matcher) {
        matchers.add(MatchCountingMatcher.countingMatcher(allOf(methodMatching(method), matcher)));
        return this;
    }
}
