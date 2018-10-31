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
package com.stehno.ersatz.issues

import spock.lang.Specification

import static com.stehno.ersatz.impl.Delegator.delegateTo
import static com.stehno.ersatz.issues.Config.conf
import static groovy.lang.Closure.DELEGATE_FIRST

class DSLScopingSpec extends Specification {

    private static final String VALUE_A = 'value-A'
    private static final String VALUE_B = 'value-B'
    private static final String VALUE_C = 'value-C'

    void 'static field should be resolvable in DSL'() {
        when:
        def config = conf {
            value VALUE_A
            nested {
                value VALUE_B
                stored {
                    value VALUE_C
                }
            }
        }

        then:
        config.value == VALUE_A
        config.valuator.value == VALUE_B
        config.valuator.stored.value == VALUE_C
    }
}

class Config {

    private String stored
    private Valuator valuator

    static Config conf(@DelegatesTo(value = Config, strategy = DELEGATE_FIRST) Closure closure) {
        delegateTo(new Config(), closure)
    }

    void value(String x) {
        stored = x
    }

    String getValue() {
        stored
    }

    void nested(@DelegatesTo(value = Valuator, strategy = DELEGATE_FIRST) Closure closure) {
        valuator = delegateTo(new Valuator(), closure)
    }

    Valuator getValuator() {
        valuator
    }
}

class Valuator {

    private String content
    private Stored stored

    void value(String x) {
        content = x
    }

    String getValue() {
        content
    }

    void stored(@DelegatesTo(value = Stored, strategy = DELEGATE_FIRST) Closure closure) {
        stored = delegateTo(new Stored(), closure)
    }

    Stored getStored() {
        stored
    }
}

class Stored {

    private String data

    void value(String x) {
        data = x
    }

    String getValue() { data }
}



