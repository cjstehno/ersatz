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

import com.stehno.ersatz.encdec.RequestDecoders
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.stream.Stream

import static com.stehno.ersatz.cfg.ContentType.APPLICATION_JSON
import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.params.provider.Arguments.arguments

class RequestDecodersTest {

    private RequestDecoders decoders

    @BeforeEach void beforeEach() {
        decoders = new RequestDecoders({
            register TEXT_PLAIN, { b, dc -> 'text/plain' }
            register 'text/plain; charset=utf-8', { b, dc -> 'text/plain; charset=utf-8' }
            register 'text/html', { b, dc -> 'text/html' }
            register 'image/png', { b, dc -> 'image/png' }
        })
    }

    @ParameterizedTest @MethodSource('contentTypeProvider')
    @DisplayName('converters(#type)') void converters(final Object type, final String result) {
        assertEquals result, decoders.findDecoder(type).apply(null, null)
    }

    private static Stream<Arguments> contentTypeProvider() {
        Stream.of(
            arguments(TEXT_PLAIN, 'text/plain'),
            arguments('text/plain; charset=utf-8', 'text/plain; charset=utf-8'),
            arguments('text/html', 'text/html'),
            arguments('image/png', 'image/png')
        )
    }

    @Test @DisplayName('registering existing type') void registerExisting() {
        decoders.register('text/plain', { b, dc -> 'modified' })

        assertEquals 'modified', decoders.findDecoder('text/plain').apply(null, null)
    }

    @Test @DisplayName('registering new type') void registerNew() {
        decoders.register(APPLICATION_JSON, { b, dc -> 'application/json' })

        assertEquals 'application/json', decoders.findDecoder('application/json').apply(null, null)
    }
}
