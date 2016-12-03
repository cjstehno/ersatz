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

    static Function<Integer, Boolean> any() {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                true
            }
        }
    }

    static Function<Integer, Boolean> atLeast(final int min) {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                count >= min
            }
        }
    }

    static Function<Integer, Boolean> atMost(final int max) {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                count <= max
            }
        }
    }

    static Function<Integer, Boolean> exactly(final int n) {
        new Function<Integer, Boolean>() {
            @Override Boolean apply(final Integer count) {
                count == n
            }
        }
    }

    static Function<Integer, Boolean> once() {
        exactly(1)
    }

    static Function<Integer, Boolean> never() {
        exactly(0)
    }
}
