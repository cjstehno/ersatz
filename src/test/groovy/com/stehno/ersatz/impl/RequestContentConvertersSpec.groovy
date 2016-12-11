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
import spock.lang.Unroll

import static com.stehno.ersatz.ContentType.APPLICATION_JSON
import static com.stehno.ersatz.ContentType.TEXT_PLAIN

class RequestContentConvertersSpec extends Specification {

    /*
        FIXME: MultipartContentMatcher object
     */

    private final RequestContentConverters converters = new RequestContentConverters({
        register TEXT_PLAIN, { b -> 'text/plain' }
        register 'text/plain; charset=utf-8', { b -> 'text/plain; charset=utf-8' }
        register 'text/html', { b -> 'text/html' }
        register 'image/png', { b -> 'image/png' }
    })

    @Unroll def 'converters(#type)'() {
        expect:
        converters.findConverter(type).apply(null) == result

        where:
        type                        || result
        TEXT_PLAIN                  || 'text/plain'
        'text/plain; charset=utf-8' || 'text/plain; charset=utf-8'
        'text/html'                 || 'text/html'
        'image/png'                 || 'image/png'
    }

    def 'registering existing type'() {
        when:
        converters.register('text/plain', { b -> 'modified' })

        then:
        converters.findConverter('text/plain').apply(null) == 'modified'
    }

    def 'registering new type'() {
        when:
        converters.register(APPLICATION_JSON, { b -> 'application/json' })

        then:
        converters.findConverter('application/json').apply(null) == 'application/json'
    }
}
