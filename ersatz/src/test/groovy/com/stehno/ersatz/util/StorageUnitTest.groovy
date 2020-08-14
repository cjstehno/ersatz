/*
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
package com.stehno.ersatz.util

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.stream.Stream

import static com.stehno.ersatz.util.StorageUnit.*
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.params.provider.Arguments.arguments

class StorageUnitTest {

    @ParameterizedTest @DisplayName('converting') @MethodSource('convertingProvider')
    void converting(final StorageUnit source, final StorageUnit desired, final Number converted) {
        assertEquals converted, desired.convert(4242, source)
    }

    private static Stream<Arguments> convertingProvider() {
        Stream.of(
            arguments(BYTES, BYTES, 4242),
            arguments(BYTES, KILOBYTES, 4.142578125),
            arguments(BYTES, MEGABYTES, 0.0040454864501953125),
            arguments(BYTES, GIGABYTES, 3.95067036151886E-6),
            arguments(BYTES, TERABYTES, 3.8580765249207616E-9),

            arguments(KILOBYTES, BYTES, 4343808),
            arguments(KILOBYTES, KILOBYTES, 4242),
            arguments(KILOBYTES, MEGABYTES, 4.142578125),
            arguments(KILOBYTES, GIGABYTES, 0.0040454864501953125),
            arguments(KILOBYTES, TERABYTES, 3.95067036151886E-6),

            arguments(MEGABYTES, BYTES, 4448059392),
            arguments(MEGABYTES, KILOBYTES, 4343808),
            arguments(MEGABYTES, MEGABYTES, 4242),
            arguments(MEGABYTES, GIGABYTES, 4.142578125),
            arguments(MEGABYTES, TERABYTES, 0.0040454864501953125),

            arguments(GIGABYTES, BYTES, 4554812817408),
            arguments(GIGABYTES, KILOBYTES, 4448059392),
            arguments(GIGABYTES, MEGABYTES, 4343808),
            arguments(GIGABYTES, GIGABYTES, 4242),
            arguments(GIGABYTES, TERABYTES, 4.142578125),

            arguments(TERABYTES, BYTES, 4664128325025792),
            arguments(TERABYTES, KILOBYTES, 4554812817408),
            arguments(TERABYTES, MEGABYTES, 4448059392),
            arguments(TERABYTES, GIGABYTES, 4343808),
            arguments(TERABYTES, TERABYTES, 4242),
        )
    }

    @ParameterizedTest @DisplayName('converting double') @MethodSource('convertingDoubleProvider')
    void convertingDouble(final StorageUnit source, final StorageUnit desired, final Number converted) {
        assertEquals converted, desired.convert(42.42d, source)
    }

    private static Stream<Arguments> convertingDoubleProvider() {
        Stream.of(
            arguments(BYTES, BYTES, 42.42),
            arguments(BYTES, KILOBYTES, 0.04142578125),
            arguments(BYTES, MEGABYTES, 0.000040454864501953125),
            arguments(BYTES, GIGABYTES, 3.95067036151886E-8),
            arguments(BYTES, TERABYTES, 3.8580765249207616E-11),

            arguments(KILOBYTES, BYTES, 43438.08),
            arguments(KILOBYTES, KILOBYTES, 42.42),
            arguments(KILOBYTES, MEGABYTES, 0.04142578125),
            arguments(KILOBYTES, GIGABYTES, 0.000040454864501953125),
            arguments(KILOBYTES, TERABYTES, 3.95067036151886E-8),

            arguments(MEGABYTES, BYTES, 4.448059392E7),
            arguments(MEGABYTES, KILOBYTES, 43438.08),
            arguments(MEGABYTES, MEGABYTES, 42.42),
            arguments(MEGABYTES, GIGABYTES, 0.04142578125),
            arguments(MEGABYTES, TERABYTES, 4.045486450195313E-5),

            arguments(GIGABYTES, BYTES, 4.554812817408E10),
            arguments(GIGABYTES, KILOBYTES, 4.448059392E7),
            arguments(GIGABYTES, MEGABYTES, 43438.08),
            arguments(GIGABYTES, GIGABYTES, 42.42),
            arguments(GIGABYTES, TERABYTES, 0.04142578125),

            arguments(TERABYTES, BYTES, 4.664128325025792E13),
            arguments(TERABYTES, KILOBYTES, 4.554812817408E10),
            arguments(TERABYTES, MEGABYTES, 4.448059392E7),
            arguments(TERABYTES, GIGABYTES, 43438.08),
            arguments(TERABYTES, TERABYTES, 42.42)
        )
    }

    @ParameterizedTest @DisplayName('approximate') @MethodSource('approximateProvider')
    void approximate(final StorageUnit source, final StorageUnit desired, final Number converted) {
        assertEquals converted, desired.approximate(4242, source)
    }

    private static Stream<Arguments> approximateProvider() {
        Stream.of(
            arguments(BYTES, BYTES, 4242),
            arguments(BYTES, KILOBYTES, 4),
            arguments(BYTES, MEGABYTES, 0),
            arguments(BYTES, GIGABYTES, 0),
            arguments(BYTES, TERABYTES, 0),

            arguments(KILOBYTES, BYTES, 4343808),
            arguments(KILOBYTES, KILOBYTES, 4242),
            arguments(KILOBYTES, MEGABYTES, 4),
            arguments(KILOBYTES, GIGABYTES, 0),
            arguments(KILOBYTES, TERABYTES, 0),

            arguments(MEGABYTES, BYTES, 4448059392),
            arguments(MEGABYTES, KILOBYTES, 4343808),
            arguments(MEGABYTES, MEGABYTES, 4242),
            arguments(MEGABYTES, GIGABYTES, 4),
            arguments(MEGABYTES, TERABYTES, 0),

            arguments(GIGABYTES, BYTES, 4554812817408),
            arguments(GIGABYTES, KILOBYTES, 4448059392),
            arguments(GIGABYTES, MEGABYTES, 4343808),
            arguments(GIGABYTES, GIGABYTES, 4242),
            arguments(GIGABYTES, TERABYTES, 4),

            arguments(TERABYTES, BYTES, 4664128325025792),
            arguments(TERABYTES, KILOBYTES, 4554812817408),
            arguments(TERABYTES, MEGABYTES, 4448059392),
            arguments(TERABYTES, GIGABYTES, 4343808),
            arguments(TERABYTES, TERABYTES, 4242)
        )
    }
}