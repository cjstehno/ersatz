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
        'any'     | '/a'             || clientRequest(PUT, '/a')
        'any'     | startsWith('/a') || clientRequest(DELETE, '/alpha')
        'get'     | '/b'             || clientRequest(GET, '/b')
        'get'     | startsWith('/b') || clientRequest(GET, '/bravo')
        'head'    | '/c'             || clientRequest(HEAD, '/c')
        'head'    | startsWith('/c') || clientRequest(HEAD, '/charlie')
        'put'     | '/d'             || clientRequest(PUT, '/d')
        'put'     | startsWith('/d') || clientRequest(PUT, '/delta')
        'post'    | '/e'             || clientRequest(POST, '/e')
        'post'    | startsWith('/e') || clientRequest(POST, '/echo')
        'delete'  | '/f'             || clientRequest(DELETE, '/f')
        'delete'  | startsWith('/f') || clientRequest(DELETE, '/foxtrot')
        'patch'   | '/g'             || clientRequest(PATCH, '/g')
        'patch'   | startsWith('/g') || clientRequest(PATCH, '/golf')
        'options' | '/h'             || clientRequest(OPTIONS, '/h')
        'options' | startsWith('/h') || clientRequest(OPTIONS, '/hotel')
    }

    private static ClientRequest clientRequest(final HttpMethod method, final String path) {
        new MockClientRequest(method: method, path: path)
    }
}
