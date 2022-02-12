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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsMapContaining.hasKey;

/**
 * Matcher for matching the existence of a key in a map.
 */
public class MapKeyMatcher extends BaseMatcher<ClientRequest> {

    private final String label;
    private final Matcher<?> keyMatcher;
    private final Function<ClientRequest, Map<String, ?>> mapProvider;

    /**
     * Creates a map key matcher.
     *
     * @param label the description prefix (label)
     * @param nameMatcher the key name matcher
     * @param negated whether the match is negated
     * @param mapProvider the provider of the data map
     */
    public MapKeyMatcher(final String label, final Matcher<String> nameMatcher, final boolean negated, final Function<ClientRequest, Map<String, ?>> mapProvider) {
        this.label = label;
        this.keyMatcher = negated ? not(hasKey(nameMatcher)) : hasKey(nameMatcher);
        this.mapProvider = mapProvider;
    }

    @Override public boolean matches(final Object actual) {
        return keyMatcher.matches(mapProvider.apply((ClientRequest) actual));
    }

    @Override public void describeTo(Description description) {
        description.appendText(label + " name is ");
        keyMatcher.describeTo(description);
    }
}
