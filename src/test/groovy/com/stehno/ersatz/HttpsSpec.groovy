/*
 * Copyright (C) 2018 Christopher J. Stehno
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
import spock.lang.AutoCleanup
import spock.lang.Specification

class HttpsSpec extends Specification {

    private final HttpClient http = new HttpClient(true)

    @AutoCleanup('stop') private final ErsatzServer ersatzServer = new ErsatzServer({
        https()
    })

    def 'https'() {
        setup:
        ersatzServer.expectations {
            get('/hello').protocol('https').responds().body('This is HTTPS!')
        }

        when:
        def response = http.get("${ersatzServer.httpsUrl}/hello")

        then:
        response.body().string() == 'This is HTTPS!'
    }
}