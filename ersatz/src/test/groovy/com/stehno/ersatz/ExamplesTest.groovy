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


import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * These tests are pulled from the various examples in the project documents.
 */
class ExamplesTest {

    @Test @DisplayName('index.html example')
    void indexExample() {
        ErsatzServer ersatz = new ErsatzServer()

        ersatz.expectations {
            GET('/say/hello') {
                called 1
                query 'name', 'Ersatz'
                responder {
                    body 'Hello Ersatz', 'text/plain'
                }
            }
        }

        URL url = "${ersatz.httpUrl}/say/hello?name=Ersatz".toURL()

        assertEquals 'Hello Ersatz', url.text
        assertTrue ersatz.verify()

        ersatz.stop()
    }

    @Test @DisplayName('say hello')
    void sayHello() {
        ErsatzServer ersatz = new ErsatzServer()

        ersatz.expectations {
            GET('/say/hello') {
                called 1
                query 'name', 'Ersatz'
                responder {
                    body 'Hello Ersatz', 'text/plain'
                }
            }
        }

        String result = "${ersatz.httpUrl}/say/hello?name=Ersatz".toURL().text

        assertEquals 'Hello Ersatz', result
        assertTrue ersatz.verify()

        ersatz.stop()
    }
}
