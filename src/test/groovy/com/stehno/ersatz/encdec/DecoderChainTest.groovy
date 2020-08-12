/*
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.encdec

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import static com.stehno.ersatz.cfg.ContentType.*
import static org.junit.jupiter.api.Assertions.assertEquals

class DecoderChainTest {

    @Test @DisplayName('chain') void chain() {
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

        DecoderChain chain = new DecoderChain(global)
        chain.first(local)
        chain.second(shared)

        assertEquals 3, chain.size()

        assertEquals local, chain[0]
        assertEquals shared, chain[1]
        assertEquals global, chain[2]

        assertEquals 'bravo-local', chain.resolve('text/alternate').apply(null, null)
        assertEquals 'echo-local', chain.resolve('application/date').apply(null, null)

        assertEquals 'delta-shared', chain.resolve(APPLICATION_JSON).apply(null, null)
        assertEquals 'alpha-shared', chain.resolve(TEXT_PLAIN).apply(null, null)

        assertEquals 'foxtrot-global', chain.resolve(APPLICATION_XML).apply(null, null)
    }
}
