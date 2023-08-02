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
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.ANY;
import static java.util.Arrays.stream;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * A matcher used to match the HTTP method of a request.
 */
@RequiredArgsConstructor(staticName = "methodMatching")
public class HttpMethodMatcher extends BaseMatcher<ClientRequest> {

    private final Matcher<HttpMethod> matcher;

    /**
     * Creates a matcher to match any o the specified request methods.
     *
     * @param methods the request methods allowed for a match
     * @return the method matcher
     */
    @SuppressWarnings("unchecked")
    public static HttpMethodMatcher methodMatching(final HttpMethod... methods) {
        return methodMatching(
            anyOf(
                stream(methods)
                    .map(m -> m == ANY ? any(HttpMethod.class) : equalTo(m))
                    .toList().toArray(new Matcher[0])
            )
        );
    }

    @Override public boolean matches(final Object actual) {
        return matcher.matches(((ClientRequest) actual).getMethod());
    }

    @Override public void describeTo(final Description description) {
        description.appendText("HTTP method is ");
        matcher.describeTo(description);
    }
}
