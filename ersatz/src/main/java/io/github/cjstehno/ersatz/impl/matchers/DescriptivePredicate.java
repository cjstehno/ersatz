package io.github.cjstehno.ersatz.impl.matchers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

/**
 * Extension of <code>Predicate</code> that allows the specification of a string of descriptive text.
 *
 * @param <T> the value being tested
 */
@RequiredArgsConstructor
public class DescriptivePredicate<T> implements Predicate<T> {

    /**
     * The descriptive text to be used.
     */
    @Getter private final String description;
    private final Predicate<T> predicate;

    @Override public boolean test(final T t) {
        return predicate.test(t);
    }
}
