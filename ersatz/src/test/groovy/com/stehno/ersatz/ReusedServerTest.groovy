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
package com.stehno.ersatz

import com.stehno.ersatz.junit.ErsatzServerExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(ErsatzServerExtension.class)
class ReusedServerTest {

    private ErsatzServer ersatzServer

    @BeforeEach void beforeEach() {
        ersatzServer.expectations {
            GET('/alpha').called(1).responds().body('alpha-response', TEXT_PLAIN)
            GET('/bravo').called(2).responds().body('bravo-response', TEXT_PLAIN)
        }
    }

    @Test @DisplayName('expected calls') void expectedCalls() {
        String resp1 = request('/alpha')
        String resp2 = request('/bravo')
        String resp3 = request('/bravo')

        assertEquals 'alpha-response', resp1
        assertEquals 'bravo-response', resp2
        assertEquals 'bravo-response', resp3
        assertTrue ersatzServer.verify()
    }

    @Test @DisplayName('clear expectations and they should be not-found') void clearShouldNotBeFound() {
        ersatzServer.clearExpectations()

        assertThrows(FileNotFoundException.class, { request('/alpha') })

        assertThrows(FileNotFoundException.class, { request('/bravo') })
    }

    @Test @DisplayName('clear expectations and add new ones') void clearAndAdd() {
        ersatzServer.clearExpectations()

        ersatzServer.expectations {
            GET('/charlie').called(1).responds().body('charlie-response', TEXT_PLAIN)
        }

        assertEquals 'charlie-response', request('/charlie')
    }

    @Test @DisplayName('same calls again to ensure that server resets normally') void sameCallsAgain(){
        String resp1 = request('/alpha')
        String resp2 = request('/bravo')
        String resp3 = request('/bravo')

        assertEquals 'alpha-response', resp1
        assertEquals 'bravo-response', resp2
        assertEquals 'bravo-response', resp3
        assertTrue ersatzServer.verify()
    }

    private String request(final String path) {
        "${ersatzServer.httpUrl}${path}".toURL().text
    }
}
