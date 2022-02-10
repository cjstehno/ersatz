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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cjstehno.ersatz.match.ErsatzMatchers.pathMatcher;

/**
 * Defines the available GET request expectations.
 */
public interface GetExpectations {

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param path the expected request path
     * @return a <code>Request</code> configuration object
     */
    default Request GET(String path) {
        return GET(pathMatcher(path));
    }

    /**
     * Allows configuration of a GET request expectation.
     *
     * @param matcher the path matcher.
     * @return a <code>Request</code> configuration object
     */
    default Request GET(Matcher<String> matcher) {
        return GET(matcher, (Consumer<Request>) null);
    }

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The <code>Consumer&lt;Request&gt;</code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param path   the expected request path
     * @param config the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    default Request GET(String path, Consumer<Request> config) {
        return GET(pathMatcher(path), config);
    }

    /**
     * Allows configuration of a GET request expectation using the provided <code>Consumer&lt;Request&gt;</code>. The <code>Consumer&lt;Request&gt;</code> will
     * have an instance of <code>Request</code> passed into it for configuration.
     *
     * @param matcher the path matcher
     * @param config  the configuration consumer
     * @return a <code>Request</code> configuration object
     */
    Request GET(final Matcher<String> matcher, final Consumer<Request> config);

    // FIXME: this is WIP
    default Request GET(final Predicate<String> predicate, final Consumer<Request> config) {
        return GET(new PredicateMatcher<>(predicate), config);
    }

    default Request GET(final String description, final Predicate<String> predicate, final Consumer<Request> config) {
        return GET(new PredicateMatcher<>(predicate, description), config);
    }
}

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
