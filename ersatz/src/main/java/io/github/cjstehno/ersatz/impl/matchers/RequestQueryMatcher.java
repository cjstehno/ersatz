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
package io.github.cjstehno.ersatz.impl.matchers;

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.val;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayDeque;

import static java.util.Arrays.asList;

public class RequestQueryMatcher extends BaseMatcher<ClientRequest> {

    private final String name;
    private final Matcher<Iterable<? super String>> matcher;

    public RequestQueryMatcher(final String name, final Matcher<Iterable<? super String>> matcher) {
        this.name = name;
        this.matcher = matcher;
    }

    @Override public boolean matches(final Object actual) {
        val clientRequest = (ClientRequest) actual;
        val queryParams = clientRequest.getQueryParams();

        if (queryParams.containsKey(name)) {
            return matcher.matches(new ArrayDeque<>(asList(queryParams.get(name).toArray(new String[0]))));
        } else {
            return false;
        }
    }

    @Override public void describeTo(final Description description) {
        description.appendText("a query string (" + name + ") matches ");
        matcher.describeTo(description);
    }
}
