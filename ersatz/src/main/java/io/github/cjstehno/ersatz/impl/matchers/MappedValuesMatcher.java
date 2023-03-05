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
package io.github.cjstehno.ersatz.impl.matchers;

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;

/**
 * A matcher used to match a map with String keys mapped to a Deque of Strings. This is a common pattern for various
 * data elements in a request.
 */
@RequiredArgsConstructor
public class MappedValuesMatcher extends BaseMatcher<ClientRequest> {

    private final String label;
    private final Matcher<String> nameMatcher;
    private final Matcher<Iterable<? super String>> valuesMatcher;
    private final Function<ClientRequest, Map<String, Deque<String>>> mapProvider;

    @Override public boolean matches(final Object actual) {
        return mapProvider.apply(((ClientRequest) actual)).entrySet().stream()
            .filter(ent -> nameMatcher.matches(ent.getKey()))
            .anyMatch(ent -> valuesMatcher.matches(new ArrayDeque<>(asList(ent.getValue().toArray(new String[0])))));
    }

    @Override public void describeTo(final Description description) {
        description.appendText(label + " name is ");
        nameMatcher.describeTo(description);
        description.appendText(" and values are ");
        valuesMatcher.describeTo(description);
    }
}
