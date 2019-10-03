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

import groovy.transform.Immutable

import java.util.concurrent.TimeUnit

import static com.stehno.ersatz.util.TimeSpanUnit.*

/**
 * Immutable representation of a time span, parsable from a string (e.g. "1 hour").
 */
@Immutable(knownImmutableClasses = [Number])
class TimeSpan {

    Number value
    TimeSpanUnit unit

    /**
     * Parses the provided string to create a TimeSpan value. The format of the string must be a number and a unit matching a TimeSpanUnit value.
     *
     * @param value the string value to be parsed
     * @return the parsed TimeSpan
     */
    static TimeSpan parse(String value) {
        def digits = []
        def unit = []

        value.trim().toCharArray().each { c ->
            if (c.isDigit() || c == '.') {
                digits << c
            } else {
                unit << c
            }
        }

        new TimeSpan(new BigDecimal(digits.join()), fromAbbreviation(unit.join().trim()))
    }

    /**
     * Formats the TimeSpan as a String using the TimeSpanUnit.abbreviate() method.
     *
     * @return a formatted string representation of the TimeSpan
     */
    String format() {
        "${value} ${unit.abbreviate(value > 1 || value < 1)}"
    }

    /**
     * Converts the time span value to milliseconds. At spans of more than DAYS, the conversion is approximate and based on a pure 7-day week,
     * 30-day month, and 365-day year. Generally it is not advisable to convert a span of larger scope than DAYS. Also note that span values
     * are converted to "long" values during the conversion so some precision may be lost.
     *
     * @return the time span converted to milliseconds.
     */
    long toMillis() {
        switch (unit) {
            case NANOSECONDS:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.NANOSECONDS)
            case MILLISECONDS:
                return value
            case SECONDS:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.SECONDS)
            case MINUTES:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.MINUTES)
            case HOURS:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.HOURS)
            case DAYS:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.DAYS)
            case WEEKS:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.DAYS) * 7
            case MONTHS:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.DAYS) * 30
            case YEARS:
                return TimeUnit.MILLISECONDS.convert(value as long, TimeUnit.DAYS) * 365
        }

    }

    TimeSpan plus(TimeSpan ts) {
        assert unit == ts.unit // no conversion at this point
        new TimeSpan(value + ts.value, unit)
    }

    TimeSpan minus(TimeSpan ts) {
        assert unit == ts.unit // no conversion at this point
        new TimeSpan(value - ts.value, unit)
    }

    TimeSpan multiply(Number num) {
        new TimeSpan(value * num, unit)
    }

    TimeSpan div(Number num) {
        new TimeSpan(value / num, unit)
    }

    TimeSpan next() {
        new TimeSpan(++value, unit)
    }

    TimeSpan previous() {
        new TimeSpan(--value, unit)
    }
}