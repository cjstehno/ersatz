/**
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.encdec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.unmodifiableList;

/**
 * Abstraction for an object chain which will have method calls applied from "first" to "last" until a non-null result is returned.
 */
public abstract class FunctionChain<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Creates a new chain with the provided (optional) first item. If the item is null, it will not be added.
     *
     * @param firstItem the optional first item (may be null)
     */
    protected FunctionChain(final T firstItem) {
        if (firstItem != null) {
            first(firstItem);
        }
    }

    /**
     * Adds the given item as the first in the chain (first to be checked). If there is already and item in the chain, it will be pushed back down
     * the resolution chain.
     *
     * @param item the first item
     */
    public final void first(final T item) {
        items.add(0, item);
    }

    /**
     * Adds the given item as the second in the chain (second to be checked). If there are more than one items already in the chain, the items beyond
     * the first will be pushed down the resolution chain; however, if there are no items in the chain, this will be the first.
     *
     * @param item the second item
     */
    public void second(final T item) {
        items.add(items.size() > 0 ? 1 : 0, item);
    }

    /**
     * Adds the given item as the last in the chain (last to be checked).
     *
     * @param item the first item
     */
    public void last(final T item) {
        items.add(item);
    }

    /**
     * Used by extension classes to perform the result resolution based on the closure. The closure will be passed each item and must return a value
     * or null - null will cause the next item in the chain to be tested.
     *
     * @param <F> the contained type being resolved
     * @param resolver the resolution closure
     * @return the resolved result or null
     */
    protected <F> F resolveWith(final Function<T, F> resolver) {
        return items.stream()
            .filter(i -> resolver.apply(i) != null)
            .findFirst()
            .map(resolver::apply)
            .orElse(null);
    }

    /**
     * Used to retrieve the item at the specified index.
     *
     * @param index the index
     * @return the item stored at the index (or null)
     */
    public T getAt(int index) {
        return ((T) (items.get(index)));
    }

    /**
     * Provides an immutable iterator over the items (in order).
     *
     * @return iterator over the items
     */
    public Iterable<T> items() {
        return unmodifiableList(items);
    }

    /**
     * Retrieves the number of items in the chain.
     *
     * @return the size of the chain
     */
    public int size() {
        return items.size();
    }
}
