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

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.any;

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
        return PathMatcher.pathMatching(path.equals("*") ? any(String.class) : equalTo(path));
    }

    /**
     * Configures a matcher that matches when the provided predicate function returns true (given the path value).
     * A generic description will be used to describe the match requirement.
     *
     * @param predicate the path predicate function
     * @return the path matcher
     */
    public static PathMatcher pathMatching(final Predicate<String> predicate) {
        return PathMatcher.pathMatching(new PredicateMatcher<>(predicate));
    }

    /**
     * Configures a matcher that matches when the provided predicate function returns true (given the path value).
     * The description provided will be used to describe the match requirement.
     *
     * @param description the match requirement description (for mismatch reporting)
     * @param predicate the path predicate function
     * @return the path matcher
     */
    public static PathMatcher pathMatching(final String description, final Predicate<String> predicate) {
        return PathMatcher.pathMatching(new PredicateMatcher<>(predicate, description));
    }

    @Override public boolean matches(final Object actual) {
        return matcher.matches(((ClientRequest) actual).getPath());
    }

    @Override public void describeTo(final Description description) {
        description.appendText("Path matches ");
        matcher.describeTo(description);
    }
}

// FIXME: this is still WIP - look at putting similar predicate support in other matchers
class PredicateMatcher<T> extends BaseMatcher<T> {
    // FIXME: move out and test

    private final Predicate<T> predicate;
    private final String description;

    public PredicateMatcher(final Predicate<T> predicate) {
        this(predicate, "a configured predicate");
    }

    public PredicateMatcher(final Predicate<T> predicate, final String description) {
        this.predicate = predicate;
        this.description = description;
    }

    @Override public boolean matches(final Object actual) {
        return predicate.test((T) actual);
    }

    @Override public void describeTo(final Description desc) {
        desc.appendText(description);
    }
}