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
package com.stehno.ersatz

import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

class ReusedServerSpec extends Specification {

    @AutoCleanup private ErsatzServer ersatzServer = new ErsatzServer()

    def setup(){
        ersatzServer.expectations {
            GET('/alpha').called(1).responds().body('alpha-response', TEXT_PLAIN)
            GET('/bravo').called(2).responds().body('bravo-response', TEXT_PLAIN)
        }
    }

    def 'expected calls'() {
        when:
        String resp1 = request('/alpha')
        String resp2 = request('/bravo')
        String resp3 = request('/bravo')

        then:
        resp1 == 'alpha-response'
        resp2 == 'bravo-response'
        resp3 == 'bravo-response'

        and:
        ersatzServer.verify()
    }

    def 'clear expectations and they should be not-found'() {
        setup:
        ersatzServer.clearExpectations()

        when:
        request('/alpha')

        then:
        thrown(FileNotFoundException)

        when:
        request('/bravo')

        then:
        thrown(FileNotFoundException)
    }

    def 'clear expectations and add new ones'() {
        setup:
        ersatzServer.clearExpectations()

        ersatzServer.expectations {
            GET('/charlie').called(1).responds().body('charlie-response', TEXT_PLAIN)
        }

        expect:
        request('/charlie') == 'charlie-response'
    }

    def 'same calls again to ensure that server resets normally'() {
        when:
        String resp1 = request('/alpha')
        String resp2 = request('/bravo')
        String resp3 = request('/bravo')

        then:
        resp1 == 'alpha-response'
        resp2 == 'bravo-response'
        resp3 == 'bravo-response'

        and:
        ersatzServer.verify()
    }

    private String request(final String path) {
        "${ersatzServer.httpUrl}${path}".toURL().text
    }
}
