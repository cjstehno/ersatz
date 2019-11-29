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
package com.stehno.ersatz.proxy.impl

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.HttpMethod
import com.stehno.ersatz.impl.MockClientRequest
import spock.lang.Specification
import spock.lang.Unroll

import static com.stehno.ersatz.HttpMethod.*
import static org.hamcrest.Matchers.startsWith

class ProxyExpectationsImplSpec extends Specification {

    @Unroll 'expectations (#method)'() {
        setup:
        ProxyExpectationsImpl expectations = new ProxyExpectationsImpl()

        when:
        expectations."$method"(path)

        then:
        expectations.matchers[0].matches(matches)

        where:
        method    | path             || matches
        'ANY'     | '/a'             || clientRequest(PUT, '/a')
        'ANY'     | startsWith('/a') || clientRequest(DELETE, '/alpha')
        'GET'     | '/b'             || clientRequest(GET, '/b')
        'GET'     | startsWith('/b') || clientRequest(GET, '/bravo')
        'HEAD'    | '/c'             || clientRequest(HEAD, '/c')
        'HEAD'    | startsWith('/c') || clientRequest(HEAD, '/charlie')
        'PUT'     | '/d'             || clientRequest(PUT, '/d')
        'PUT'     | startsWith('/d') || clientRequest(PUT, '/delta')
        'POST'    | '/e'             || clientRequest(POST, '/e')
        'POST'    | startsWith('/e') || clientRequest(POST, '/echo')
        'DELETE'  | '/f'             || clientRequest(DELETE, '/f')
        'DELETE'  | startsWith('/f') || clientRequest(DELETE, '/foxtrot')
        'PATCH'   | '/g'             || clientRequest(PATCH, '/g')
        'PATCH'   | startsWith('/g') || clientRequest(PATCH, '/golf')
        'OPTIONS' | '/h'             || clientRequest(OPTIONS, '/h')
        'OPTIONS' | startsWith('/h') || clientRequest(OPTIONS, '/hotel')
    }

    private static ClientRequest clientRequest(final HttpMethod method, final String path) {
        new MockClientRequest(method: method, path: path)
    }
}
