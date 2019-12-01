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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.cfg.HttpMethod
import com.stehno.ersatz.impl.MockClientRequest
import com.stehno.ersatz.cfg.impl.ProxyExpectationsImpl
import spock.lang.Specification
import spock.lang.Unroll

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
        'ANY'     | '/a'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.PUT, '/a')
        'ANY'     | startsWith('/a') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.DELETE, '/alpha')
        'GET'     | '/b'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.GET, '/b')
        'GET'     | startsWith('/b') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.GET, '/bravo')
        'HEAD'    | '/c'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.HEAD, '/c')
        'HEAD'    | startsWith('/c') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.HEAD, '/charlie')
        'PUT'     | '/d'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.PUT, '/d')
        'PUT'     | startsWith('/d') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.PUT, '/delta')
        'POST'    | '/e'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.POST, '/e')
        'POST'    | startsWith('/e') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.POST, '/echo')
        'DELETE'  | '/f'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.DELETE, '/f')
        'DELETE'  | startsWith('/f') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.DELETE, '/foxtrot')
        'PATCH'   | '/g'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.PATCH, '/g')
        'PATCH'   | startsWith('/g') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.PATCH, '/golf')
        'OPTIONS' | '/h'             || clientRequest(com.stehno.ersatz.cfg.HttpMethod.OPTIONS, '/h')
        'OPTIONS' | startsWith('/h') || clientRequest(com.stehno.ersatz.cfg.HttpMethod.OPTIONS, '/hotel')
    }

    private static ClientRequest clientRequest(final HttpMethod method, final String path) {
        new MockClientRequest(method: method, path: path)
    }
}
