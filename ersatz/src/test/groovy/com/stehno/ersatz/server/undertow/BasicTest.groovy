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
import com.stehno.ersatz.junit.ErsatzServerExtension
import com.stehno.ersatz.util.HttpClient
import okhttp3.Response
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static com.stehno.ersatz.server.undertow.SimpleIdentityManager.encodedCredential
import static org.junit.jupiter.api.Assertions.assertEquals

@ExtendWith(ErsatzServerExtension)
class BasicTest {

    private ErsatzServer ersatzServer = new ErsatzServer({
        authentication {
            basic()
        }
    })

    @Test @DisplayName('BASIC auth') void basicAuth() {
        ersatzServer.expectations {
            GET('/secrets').responds().body('Something secure')
        }

        def http = new HttpClient()

        Response response = http.get(ersatzServer.httpUrl('/secrets'))

        assertEquals 401, response.code()
        assertEquals '', response.body().string()

        response = http.get(ersatzServer.httpUrl('/secrets'), Authorization: encodedCredential('admin', '$3cr3t'))

        assertEquals 200, response.code()
        assertEquals 'Something secure', response.body().string()
    }
}
