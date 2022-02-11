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

import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.function.Predicate;

/**
 * Matcher used to match based on the result of a Predicate.
 */
@RequiredArgsConstructor
public class PredicateMatcher<T> extends BaseMatcher<T> {

    private final Predicate<T> predicate;
    private final String description;

    public PredicateMatcher(final Predicate<T> predicate) {
        this(predicate, "a configured predicate");
    }

    @Override public boolean matches(final Object actual) {
        return predicate.test((T) actual);
    }

    @Override public void describeTo(final Description desc) {
        desc.appendText(description);
    }
}
