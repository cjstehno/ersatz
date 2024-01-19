/**
 * Copyright (C) 2024 Christopher J. Stehno
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

import io.github.cjstehno.ersatz.cfg.RequestRequirement;
import io.github.cjstehno.ersatz.impl.matchers.RequestSchemeMatcher;
import io.github.cjstehno.ersatz.match.HeaderMatcher;
import io.github.cjstehno.ersatz.match.HttpMethodMatcher;
import io.github.cjstehno.ersatz.match.PathMatcher;
import io.github.cjstehno.ersatz.match.QueryParamMatcher;
import io.github.cjstehno.ersatz.match.RequestCookieMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;

/**
 * Implementation of a request requirement.
 */
@RequiredArgsConstructor
public class ErsatzRequestRequirement implements RequestRequirement {

    @Getter private final List<Matcher<ClientRequest>> matchers = new LinkedList<>();
    @Getter private final HttpMethodMatcher methodMatcher;
    @Getter private final PathMatcher pathMatcher;

    @Override public RequestRequirement secure(final boolean enabled) {
        matchers.add(new RequestSchemeMatcher(enabled));
        return this;
    }

    @Override public RequestRequirement header(final HeaderMatcher headerMatcher) {
        matchers.add(headerMatcher);
        return this;
    }

    @Override public RequestRequirement query(final QueryParamMatcher queryMatcher) {
        matchers.add(queryMatcher);
        return this;
    }

    @Override public RequestRequirement cookie(final RequestCookieMatcher cookieMatcher) {
        matchers.add(cookieMatcher);
        return this;
    }

    @Override public RequestRequirement matcher(final Matcher<ClientRequest> matcher) {
        matchers.add(matcher);
        return this;
    }

    /**
     * Determines whether this requirement should be applied to the provided request.
     *
     * @param clientRequest the request
     * @return a value of true if this requirement should be applied
     */
    public boolean matches(final ClientRequest clientRequest) {
        return allOf(methodMatcher, pathMatcher).matches(clientRequest);
    }

    /**
     * Checks the request against this requirement, returning true if the configured conditions are met.
     *
     * @param clientRequest the request being compared to the requirements
     * @return a value of true if the request meets the requirements
     */
    public boolean check(final ClientRequest clientRequest) {
        return allOf(matchers.toArray(new Matcher[0])).matches(clientRequest);
    }

    /**
     * Used to render a meaningful description of the requirement matcher (method and path).
     *
     * @return the description
     */
    public String getDescription(){
        val desc = new StringDescription();
        methodMatcher.describeTo(desc);
        desc.appendText(" & ");
        pathMatcher.describeTo(desc);
        return desc.toString();
    }
}
