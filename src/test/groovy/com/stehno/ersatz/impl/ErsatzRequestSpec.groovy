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
package com.stehno.ersatz.impl

import spock.lang.Specification

class ErsatzRequestSpec extends Specification {

    private final ErsatzRequest request = new ErsatzRequest('TEST', '/testing')

    def 'method and path'() {
        expect:
        request.method == 'TEST' && request.path == '/testing'
    }

    def 'to string'() {
        expect:
        request.toString() == '{ TEST /testing (query=[:], headers=[:], cookies=[:]): counted=0 }'
    }

    def 'headers'() {
        when:
        request.headers(alpha: 'bravo', charlie: 'delta').header('echo', 'foxtrot')

        then:
        request.getHeader('alpha') == 'bravo'
        request.getHeader('charlie') == 'delta'
        request.getHeader('echo') == 'foxtrot'
    }
}
