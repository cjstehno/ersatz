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
package com.stehno.ersatz.issues

import space.jasan.support.groovy.closure.ConsumerWithDelegate
import spock.lang.Specification

import static com.stehno.ersatz.issues.Config.conf
import static groovy.lang.Closure.DELEGATE_FIRST

class DSLScopingSpec extends Specification {

    private static final String VALUE_A = 'value-A'
    private static final String VALUE_B = 'value-B'
    private static final String VALUE_C = 'value-C'

    void 'static field should be resolvable in DSL'() {
        setup:
        def config = conf {
            value VALUE_A
            nested {
                value VALUE_B
                stored {
                    value VALUE_C
                }
            }
        }

        expect:
        config.value == VALUE_A
        config.valuator.value == VALUE_B
        config.valuator.stored.value == VALUE_C
    }
}

class Config {

    private String stored
    private Valuator valuator

    static Config conf(@DelegatesTo(value = Config, strategy = DELEGATE_FIRST) Closure closure) {
        Config config = new Config()
        ConsumerWithDelegate.create(closure).accept(config)
        config
    }

    void value(String x) {
        stored = x
    }

    String getValue() {
        stored
    }

    void nested(@DelegatesTo(value = Valuator, strategy = DELEGATE_FIRST) Closure closure) {
        Valuator valuator = new Valuator()
        ConsumerWithDelegate.create(closure).accept(valuator)
        this.valuator = valuator
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
        Stored stored = new Stored()
        ConsumerWithDelegate.create(closure).accept(stored)
        this.stored = stored
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



