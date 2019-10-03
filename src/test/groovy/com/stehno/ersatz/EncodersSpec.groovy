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

import spock.lang.Specification
import spock.lang.Unroll

class EncodersSpec extends Specification {

    @Unroll 'json: #data'() {
        expect:
        Encoders.json.apply(data) == result

        where:
        data                     || result
        null                     || '{}'
        [:]                      || '{}'
        [one: 'alpha', two: 222] || '{"one":"alpha","two":222}'
    }

    @Unroll 'text: #data'() {
        expect:
        Encoders.text.apply(data) == result

        where:
        data                    || result
        null                    || ''
        ''                      || ''
        'something interesting' || 'something interesting'
        42                      || '42'
    }

    @Unroll 'binaryBase64'() {
        expect:
        Encoders.binaryBase64.apply(data) == result

        where:
        data                                         || result
        null                                         || ''
        [] as byte[]                                 || ''
        'some bytes'.bytes                           || 'c29tZSBieXRlcw=='
        new ByteArrayInputStream('more bytes'.bytes) || 'bW9yZSBieXRlcw=='
    }
}
