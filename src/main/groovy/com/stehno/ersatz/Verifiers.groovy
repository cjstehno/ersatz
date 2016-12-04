/*
 * Copyright (C) 2016 Christopher J. Stehno
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
package com.stehno.ersatz

import groovy.transform.CompileStatic

import java.util.function.Function

/**
 * Some standard verifier functions.
 */
@CompileStatic
class Verifiers {

    /**
     * Verifies that the request was called any number of times.
     */
    static Function<Integer, Boolean> any() {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                true
            }
        }
    }

    /**
     * Verifies that the request was called at least the specified number of times.
     */
    static Function<Integer, Boolean> atLeast(final int min) {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                count >= min
            }
        }
    }

    /**
     * Verifies that the request was called at most the specified number of times.
     */
    static Function<Integer, Boolean> atMost(final int max) {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                count <= max
            }
        }
    }

    /**
     * Verifies that the request was called exactly the specified number of times.
     */
    static Function<Integer, Boolean> exactly(final int n) {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                count == n
            }
        }
    }

    /**
     * Verifies that the request was called only once. This is the same as calling <code>exactly(1)</code>.
     */
    static Function<Integer, Boolean> once() {
        exactly(1)
    }

    /**
     * Verifies that the request was never called. This is the same as calling <code>exactly(0)</code>.
     */
    static Function<Integer, Boolean> never() {
        exactly(0)
    }
}
