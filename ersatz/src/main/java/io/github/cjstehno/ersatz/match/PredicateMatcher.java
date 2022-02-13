/**
 * Copyright (C) 2022 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.impl.matchers.DescriptivePredicate;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.function.Predicate;

/**
 * Matcher used to match based on the result of a Predicate.
 */
@RequiredArgsConstructor(staticName = "predicatedBy")
public class PredicateMatcher<T> extends BaseMatcher<T> {

    private final Predicate<T> predicate;

    public static <T> PredicateMatcher<T> predicatedBy(final String description, final Predicate<T> predicate) {
        return new PredicateMatcher<>(new DescriptivePredicate<>(description, predicate));
    }

    @Override @SuppressWarnings("unchecked")
    public boolean matches(final Object actual) {
        return predicate.test((T) actual);
    }

    @Override public void describeTo(final Description desc) {
        if (predicate instanceof DescriptivePredicate) {
            desc.appendText(((DescriptivePredicate<?>) predicate).getDescription());
        } else {
            desc.appendText("a predicate function");
        }
    }
}
