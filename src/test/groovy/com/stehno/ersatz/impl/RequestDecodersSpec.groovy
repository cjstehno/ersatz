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
package com.stehno.ersatz.impl

import com.stehno.ersatz.RequestDecoders
import spock.lang.Specification
import spock.lang.Unroll

import static com.stehno.ersatz.ContentType.APPLICATION_JSON
import static com.stehno.ersatz.ContentType.TEXT_PLAIN

class RequestDecodersSpec extends Specification {

    private final RequestDecoders decoders = new RequestDecoders({
        register TEXT_PLAIN, { b, dc -> 'text/plain' }
        register 'text/plain; charset=utf-8', { b, dc -> 'text/plain; charset=utf-8' }
        register 'text/html', { b, dc -> 'text/html' }
        register 'image/png', { b, dc -> 'image/png' }
    })

    @Unroll 'converters(#type)'() {
        expect:
        decoders.findDecoder(type).apply(null,null) == result

        where:
        type                        || result
        TEXT_PLAIN                  || 'text/plain'
        'text/plain; charset=utf-8' || 'text/plain; charset=utf-8'
        'text/html'                 || 'text/html'
        'image/png'                 || 'image/png'
    }

    def 'registering existing type'() {
        when:
        decoders.register('text/plain', { b, dc -> 'modified' })

        then:
        decoders.findDecoder('text/plain').apply(null,null) == 'modified'
    }

    def 'registering new type'() {
        when:
        decoders.register(APPLICATION_JSON, { b, dc -> 'application/json' })

        then:
        decoders.findDecoder('application/json').apply(null,null) == 'application/json'
    }
}
