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


import com.stehno.ersatz.test.Http
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static org.junit.jupiter.api.Assertions.assertEquals

class GroovyErsatzServerTest {

    /* FIxNME: test
        - groovy ctor with java config
        - groovy expectations
        - test with mixed java/groovy style
     */

    private GroovyErsatzServer server

    @AfterEach void afterEach() {
        server?.close()
    }

    @Test @DisplayName('empty ctor')
    void ctor() {
        server = new GroovyErsatzServer()

        server.expectations {
            GET('/foo') {
                header 'alpha', 'bravo'
                responder {
                    body 'Groovy, baby!', TEXT_PLAIN
                }
            }
        }

        def value = new Http(server).GET('/foo', alpha: 'bravo').body()
        assertEquals('Groovy, baby!', value)
    }

    @Test @DisplayName('ctor with config')
    void ctorConfig() {
        server = new GroovyErsatzServer({
            expectations {
                GET('/foo') {
                    header 'fun', 'times'
                    responder {
                        body 'Hello, Groovy!', TEXT_PLAIN
                    }
                }
            }
        })

        def value = new Http(server).GET('/foo', fun: 'times').body()
        assertEquals('Hello, Groovy!', value)
    }
}