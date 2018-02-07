/*
 * Copyright (C) 2018 Christopher J. Stehno
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

import static com.stehno.ersatz.util.TimeSpan.parse
import static com.stehno.ersatz.util.TimeSpanUnit.*


class TimeSpanSpec extends Specification {

    def 'spans'() {
        when:
        def span = parse(text)

        then:
        span == result

        and:
        span.format() == abbrev

        where:
        text          | abbrev              | result
        '1 hour'      | '1 hour'            | new TimeSpan(1, HOURS)
        '2h'          | '2 hours'           | new TimeSpan(2, HOURS)
        '3 h'         | '3 hours'           | new TimeSpan(3, HOURS)
        '4 hours'     | '4 hours'           | new TimeSpan(4, HOURS)
        '5 hrs'       | '5 hours'           | new TimeSpan(5, HOURS)
        '6hrs'        | '6 hours'           | new TimeSpan(6, HOURS)
        '3.1415h'     | '3.1415 hours'      | new TimeSpan(3.1415, HOURS) // it's pie time!
        ' 45 minutes' | '45 minutes'        | new TimeSpan(45, MINUTES)
        '38m'         | '38 minutes'        | new TimeSpan(38, MINUTES)
        '56s'         | '56 seconds'        | new TimeSpan(56, SECONDS)
        '1256ms'      | '1256 milliseconds' | new TimeSpan(1256, MILLISECONDS)
        '0.24ms'      | '0.24 milliseconds' | new TimeSpan(0.24, MILLISECONDS)
        '7 days'      | '7 days'            | new TimeSpan(7, DAYS)
        '52 weeks'    | '52 weeks'          | new TimeSpan(52, WEEKS)
        '1 month'     | '1 month'           | new TimeSpan(1, MONTHS)
        '42 yr'       | '42 years'          | new TimeSpan(42, YEARS)
    }

    def 'math'() {
        expect:
        input == output

        where:
        input                          || output
        parse('3h') + parse('7 hours') || new TimeSpan(10, HOURS)
        parse('6h') - parse('4 hours') || new TimeSpan(2, HOURS)
        parse('3 hrs') / 3             || new TimeSpan(1, HOURS)
        parse('3 hrs') * 3             || new TimeSpan(9, HOURS)
        parse(' 2h ')++                || new TimeSpan(3, HOURS)
        parse(' 2h ')--                || new TimeSpan(1, HOURS)
    }

    def 'millis'() {
        expect:
        span.toMillis() == millis

        where:
        span                             || millis
        new TimeSpan(1, HOURS)           || 3600000
        new TimeSpan(2, HOURS)           || 7200000
        new TimeSpan(3, HOURS)           || 10800000
        new TimeSpan(3.1415, HOURS)      || 10800000
        new TimeSpan(45, MINUTES)        || 2700000
        new TimeSpan(56, SECONDS)        || 56000
        new TimeSpan(1256, MILLISECONDS) || 1256
        new TimeSpan(0.24, MILLISECONDS) || 0
        new TimeSpan(7, DAYS)            || 604800000
        new TimeSpan(52, WEEKS)          || 31449600000
        new TimeSpan(1, MONTHS)          || 2592000000
        new TimeSpan(42, YEARS)          || 1324512000000
    }
}
