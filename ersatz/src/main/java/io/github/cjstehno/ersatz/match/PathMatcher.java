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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.any;

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Matcher used to match the request path.
 */
@RequiredArgsConstructor(staticName = "pathMatching")
public class PathMatcher extends BaseMatcher<ClientRequest> {

    private final Matcher<String> matcher;

    /**
     * Configures a matcher expecting a request path equal to the provided path. If "*" is used, it will match any
     * String value.
     *
     * @param path the expected path
     * @return the path matcher
     */
    public static PathMatcher pathMatching(final String path) {
        return pathMatching(path.equals("*") ? any(String.class) : equalTo(path));
    }

    /**
     * Configures a matcher that will match any path.
     *
     * @return the path matcher
     */
    public static PathMatcher anyPath(){
        return pathMatching(any(String.class));
    }

    @Override public boolean matches(final Object actual) {
        return matcher.matches(((ClientRequest) actual).getPath());
    }

    @Override public void describeTo(final Description description) {
        description.appendText("Path matches ");
        matcher.describeTo(description);
    }
}