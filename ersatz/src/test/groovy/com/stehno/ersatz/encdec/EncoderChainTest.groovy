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

class EncoderChainTest {

    @Test @DisplayName('chain') void chain() {
        ResponseEncoders global = new ResponseEncoders({
            register TEXT_PLAIN, String, { o -> 'alpha-global' }
            register APPLICATION_XML, String, { o -> 'bravo-global' }
            register APPLICATION_JSON, String, { o -> 'charlie-global' }
        })

        ResponseEncoders shared = new ResponseEncoders({
            register APPLICATION_XML, String, { o -> 'bravo-shared' }
            register APPLICATION_JAVASCRIPT, String, { o -> 'delta-shared' }
        })

        ResponseEncoders local = new ResponseEncoders({
            register APPLICATION_JSON, String, { o -> 'charlie-local' }
            register APPLICATION_URLENCODED, String, { o -> 'echo-local' }
        })

        EncoderChain chain = new EncoderChain(global)
        chain.first(local)
        chain.second(shared)

        assertEquals 'charlie-local', chain.resolve(APPLICATION_JSON, String).apply(null)
        assertEquals 'echo-local', chain.resolve(APPLICATION_URLENCODED, String).apply(null)
        assertEquals 'delta-shared', chain.resolve(APPLICATION_JAVASCRIPT, String).apply(null)
        assertEquals 'bravo-shared', chain.resolve(APPLICATION_XML, String).apply(null)
        assertEquals 'alpha-global', chain.resolve(TEXT_PLAIN, String).apply(null)
    }
}
