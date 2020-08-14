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

import com.stehno.ersatz.util.HttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class HttpsTest {

    private HttpClient http
    private ErsatzServer server;

    @BeforeEach void beforeEach() {
        http = new HttpClient(true)
        server = new ErsatzServer({
            https()
        })
    }

    @AfterEach void afterEach() {
        server.close()
    }

    @Test @DisplayName('Https server') void httpsServer() {
        server.expectations {
            GET('/hello').protocol('https').responds().body('This is HTTPS!')
        }

        def response = http.get(server.httpsUrl('/hello'))

        assertEquals('This is HTTPS!', response.body().string())
    }
}