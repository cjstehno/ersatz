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

/**
 * Unit of a TimeSpan.
 */
enum TimeSpanUnit {

    NANOSECONDS('ns', 'nanosecond', 'nanoseconds'),
    MILLISECONDS('ms', 'millisecond', 'milliseconds'),
    SECONDS('s', 'sec', 'secs', 'second', 'seconds'),
    MINUTES('m', 'min', 'mins', 'minute', 'minutes'),
    HOURS('h', 'hr', 'hrs', 'hour', 'hours'),
    DAYS('d', 'day', 'days'),
    WEEKS('w', 'week', 'weeks'),
    MONTHS('mo', 'month', 'months'),
    YEARS('y', 'yr', 'yrs', 'year', 'years')

    final List<String> abbreviations

    private TimeSpanUnit(String... abbrevs) {
        abbreviations = (abbrevs as List<String>).asImmutable()
    }

    String abbreviate(boolean plural) {
        abbreviations[plural ? -1 : -2]
    }

    static TimeSpanUnit fromAbbreviation(String abr) {
        values().find { u -> u.abbreviations.contains(abr.toLowerCase()) }
    }
}
