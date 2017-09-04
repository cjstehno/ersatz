/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.proxy.impl

import com.stehno.ersatz.proxy.ProxyExpectations
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Consumer

class ProxyConfigImplSpec extends Specification {

    @Unroll 'configuration (closure)'() {
        setup:
        ProxyConfigImpl config = new ProxyConfigImpl()

        when:
        config.autoStart(false).target(target).expectations {
            any('/')
        }

        then:
        !config.autoStart
        config.targetUri == uri
        config.expectations.matchers.size() == 1

        where:
        target                         | uri
        'http://localhost/foo'         | 'http://localhost/foo'.toURI()
        'http://localhost/foo'.toURI() | 'http://localhost/foo'.toURI()
        'http://localhost/foo'.toURL() | 'http://localhost/foo'.toURI()
    }

    @Unroll 'configuration (consumer)'() {
        setup:
        ProxyConfigImpl config = new ProxyConfigImpl()

        when:
        config.autoStart(true).target('http://localhost/bar').expectations(new Consumer<ProxyExpectations>() {
            @Override void accept(ProxyExpectations expects) {
                expects.any('/blah')
            }
        })

        then:
        config.autoStart
        config.targetUri == 'http://localhost/bar'.toURI()
        config.expectations.matchers.size() == 1
    }
}
