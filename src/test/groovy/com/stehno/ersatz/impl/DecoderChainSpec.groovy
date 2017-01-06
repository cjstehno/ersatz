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

import static com.stehno.ersatz.ContentType.*

class DecoderChainSpec extends Specification {

    def 'chain'() {
        setup:
        RequestDecoders global = new RequestDecoders({
            register TEXT_PLAIN, { b, ctx -> 'alpha-global' }
            register 'text/alternate', { b, ctx -> 'bravo-global' }
            register APPLICATION_JAVASCRIPT, { b, ctx -> 'charlie-global' }
            register APPLICATION_XML, { b, ctx -> 'foxtrot-global' }
        })

        RequestDecoders shared = new RequestDecoders({
            register TEXT_PLAIN, { b, ctx -> 'alpha-shared' }
            register APPLICATION_JSON, { b, ctx -> 'delta-shared' }
        })

        RequestDecoders local = new RequestDecoders({
            register 'text/alternate', { b, ctx -> 'bravo-local' }
            register 'application/date', { b, ctx -> 'echo-local' }
        })

        when:
        DecoderChain chain = new DecoderChain(global)
        chain.first(local)
        chain.second(shared)

        then:
        chain.size() == 3

        chain[0] == local
        chain[1] == shared
        chain[2] == global

        and:
        chain.resolve('text/alternate').apply(null, null) == 'bravo-local'
        chain.resolve('application/date').apply(null, null) == 'echo-local'

        chain.resolve(APPLICATION_JSON).apply(null, null) == 'delta-shared'
        chain.resolve(TEXT_PLAIN).apply(null, null) == 'alpha-shared'

        chain.resolve(APPLICATION_XML).apply(null, null) == 'foxtrot-global'
    }
}
