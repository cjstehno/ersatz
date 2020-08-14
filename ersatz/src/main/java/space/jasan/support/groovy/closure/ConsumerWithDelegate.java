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
package space.jasan.support.groovy.closure;

import groovy.lang.Closure;

import java.util.function.Consumer;

/*
    I hate to do this, but I only use a couple classes from this library
    (https://github.com/jasanspace/groovy-closure-support/blob/master/src/main/java/space/jasan/support/groovy/closure/ConsumerWithDelegate.java)
    and it does not publish to Maven Central (which I understand), but I cannot remove support for my library simply
    because they don't want to publish to the most-used (and most annoying) public repository.
 */
public class ConsumerWithDelegate<T> implements Consumer<T> {

    public static <T> Consumer<T> create(Closure c, Object owner, int strategy) {
        return new ConsumerWithDelegate<>(c, strategy, owner);
    }

    public static <T> Consumer<T> create(Closure c, Object owner) {
        return create(c, owner, Closure.DELEGATE_FIRST);
    }

    public static <T> Consumer<T> create(Closure c, int strategy) {
        return create(c, GroovyClosure.getPropagatedOwner(c.getOwner()), strategy);
    }

    public static <T> Consumer<T> create(Closure c) {
        return create(c, Closure.DELEGATE_FIRST);
    }

    private final int strategy;
    private final Object owner;
    private final Closure closure;

    private ConsumerWithDelegate(Closure closure, int strategy, Object owner) {
        this.strategy = strategy;
        this.owner = owner;
        this.closure = closure;
    }

    @Override
    public void accept(T t) {
        Closure closure = this.closure.rehydrate(t, owner, this.closure.getThisObject());
        closure.setResolveStrategy(strategy);
        closure.call(t);
    }
}