/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.util

import spock.lang.Specification
import spock.lang.Unroll

import static com.stehno.ersatz.util.StorageUnit.*

class StorageUnitSpec extends Specification {

    @Unroll
    def 'convert: #source to #desired'() {
        expect:
        desired.convert(4242, source) == converted

        where:
        source    | desired   || converted
        BYTES     | BYTES     || 4242
        BYTES     | KILOBYTES || 4.142578125
        BYTES     | MEGABYTES || 0.0040454864501953125
        BYTES     | GIGABYTES || 3.95067036151886E-6
        BYTES     | TERABYTES || 3.8580765249207616E-9

        KILOBYTES | BYTES     || 4343808
        KILOBYTES | KILOBYTES || 4242
        KILOBYTES | MEGABYTES || 4.142578125
        KILOBYTES | GIGABYTES || 0.0040454864501953125
        KILOBYTES | TERABYTES || 3.95067036151886E-6

        MEGABYTES | BYTES     || 4448059392
        MEGABYTES | KILOBYTES || 4343808
        MEGABYTES | MEGABYTES || 4242
        MEGABYTES | GIGABYTES || 4.142578125
        MEGABYTES | TERABYTES || 0.0040454864501953125

        GIGABYTES | BYTES     || 4554812817408
        GIGABYTES | KILOBYTES || 4448059392
        GIGABYTES | MEGABYTES || 4343808
        GIGABYTES | GIGABYTES || 4242
        GIGABYTES | TERABYTES || 4.142578125

        TERABYTES | BYTES     || 4664128325025792
        TERABYTES | KILOBYTES || 4554812817408
        TERABYTES | MEGABYTES || 4448059392
        TERABYTES | GIGABYTES || 4343808
        TERABYTES | TERABYTES || 4242
    }

    @Unroll
    def 'convert(double): #source to #desired'() {
        expect:
        desired.convert(42.42d, source) == converted

        where:
        source    | desired   || converted
        BYTES     | BYTES     || 42.42
        BYTES     | KILOBYTES || 0.04142578125
        BYTES     | MEGABYTES || 0.000040454864501953125
        BYTES     | GIGABYTES || 3.95067036151886E-8
        BYTES     | TERABYTES || 3.8580765249207616E-11

        KILOBYTES | BYTES     || 43438.08
        KILOBYTES | KILOBYTES || 42.42
        KILOBYTES | MEGABYTES || 0.04142578125
        KILOBYTES | GIGABYTES || 0.000040454864501953125
        KILOBYTES | TERABYTES || 3.95067036151886E-8

        MEGABYTES | BYTES     || 4.448059392E7
        MEGABYTES | KILOBYTES || 43438.08
        MEGABYTES | MEGABYTES || 42.42
        MEGABYTES | GIGABYTES || 0.04142578125
        MEGABYTES | TERABYTES || 4.045486450195313E-5

        GIGABYTES | BYTES     || 4.554812817408E10
        GIGABYTES | KILOBYTES || 4.448059392E7
        GIGABYTES | MEGABYTES || 43438.08
        GIGABYTES | GIGABYTES || 42.42
        GIGABYTES | TERABYTES || 0.04142578125

        TERABYTES | BYTES     || 4.664128325025792E13
        TERABYTES | KILOBYTES || 4.554812817408E10
        TERABYTES | MEGABYTES || 4.448059392E7
        TERABYTES | GIGABYTES || 43438.08
        TERABYTES | TERABYTES || 42.42
    }

    @Unroll
    def 'approximate: #source to #desired'() {
        expect:
        desired.approximate(4242, source) == converted

        where:
        source    | desired   || converted
        BYTES     | BYTES     || 4242
        BYTES     | KILOBYTES || 4
        BYTES     | MEGABYTES || 0
        BYTES     | GIGABYTES || 0
        BYTES     | TERABYTES || 0

        KILOBYTES | BYTES     || 4343808
        KILOBYTES | KILOBYTES || 4242
        KILOBYTES | MEGABYTES || 4
        KILOBYTES | GIGABYTES || 0
        KILOBYTES | TERABYTES || 0

        MEGABYTES | BYTES     || 4448059392
        MEGABYTES | KILOBYTES || 4343808
        MEGABYTES | MEGABYTES || 4242
        MEGABYTES | GIGABYTES || 4
        MEGABYTES | TERABYTES || 0

        GIGABYTES | BYTES     || 4554812817408
        GIGABYTES | KILOBYTES || 4448059392
        GIGABYTES | MEGABYTES || 4343808
        GIGABYTES | GIGABYTES || 4242
        GIGABYTES | TERABYTES || 4

        TERABYTES | BYTES     || 4664128325025792
        TERABYTES | KILOBYTES || 4554812817408
        TERABYTES | MEGABYTES || 4448059392
        TERABYTES | GIGABYTES || 4343808
        TERABYTES | TERABYTES || 4242
    }
}