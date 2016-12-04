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

import spock.lang.Specification
import spock.lang.Unroll

class VerifiersSpec extends Specification {

    def 'any'() {
        expect:
        Verifiers.any().apply(value)

        where:
        value << [0, 10, 100, 1000, 10000]
    }

    @Unroll def 'atLeast(#value) with #count'() {
        expect:
        Verifiers.atLeast(value).apply(count) == result

        where:
        value | count || result
        10    | 0     || false
        10    | 9     || false
        10    | 10    || true
        10    | 11    || true
        1     | 0     || false
        1     | 1     || true
        1     | 2     || true
    }

    @Unroll def 'atMost(#value) with #count'() {
        expect:
        Verifiers.atMost(value).apply(count) == result

        where:
        value | count || result
        10    | 0     || true
        10    | 9     || true
        10    | 10    || true
        10    | 11    || false
        1     | 0     || true
        1     | 1     || true
        1     | 2     || false
    }

    @Unroll def 'exactly(#value) with #count'() {
        expect:
        Verifiers.exactly(value).apply(count) == result

        where:
        value | count || result
        1     | 1     || true
        1     | 2     || false
        7     | 7     || true
        7     | 6     || false
    }

    @Unroll def 'once() with #count'() {
        expect:
        Verifiers.once().apply(count) == result

        where:
        count || result
        1     || true
        2     || false
        3     || false
    }

    @Unroll def 'never() with #count'() {
        expect:
        Verifiers.never().apply(count) == result

        where:
        count || result
        0     || true
        1     || false
        5     || false
    }
}
