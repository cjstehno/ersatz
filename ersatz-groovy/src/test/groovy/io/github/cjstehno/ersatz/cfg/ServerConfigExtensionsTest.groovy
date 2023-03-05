/*
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.cfg;

import io.github.cjstehno.ersatz.GroovyErsatzServer
import io.github.cjstehno.ersatz.test.Http;
import org.junit.jupiter.api.Test

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.*;

class ServerConfigExtensionsTest {

    @Test void expectationsExtension(){
        def server = new GroovyErsatzServer()
        server.expectations {
            GET('/foo'){
                called 1
                responder {
                    code 200
                    body 'This is Groovy', TEXT_PLAIN
                }
            }
        }

        def http = new Http(server.getHttpUrl())
        def response = http.get('/foo')

        assertEquals 200, response.statusCode()
        assertEquals 'This is Groovy', response.body()
        assertTrue server.verify()

        server.close()
    }
}