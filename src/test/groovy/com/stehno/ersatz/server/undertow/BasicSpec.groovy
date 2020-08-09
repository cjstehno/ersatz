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
package com.stehno.ersatz.server.undertow

import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.util.HttpClient
import spock.lang.Specification

import static com.stehno.ersatz.server.undertow.SimpleIdentityManager.encodedCredential

class BasicSpec extends Specification {

    private ErsatzServer ersatzServer = new ErsatzServer({
        authentication {
            basic()
        }
    })
    private HttpClient http = new HttpClient()

    def 'BASIC auth'() {
        setup:
        ersatzServer.expectations {
            GET('/secrets').responds().body('Something secure')
        }

        when:
        okhttp3.Response response = http.get("${ersatzServer.httpUrl}/secrets")

        then:
        response.code() == 401
        response.body().string() == ''

        when:
        response = http.get("${ersatzServer.httpUrl}/secrets", Authorization: encodedCredential('admin', '$3cr3t'))

        then:
        response.code() == 200
        response.body().string() == 'Something secure'
    }

    def cleanup() {
        ersatzServer.stop()
    }
}
