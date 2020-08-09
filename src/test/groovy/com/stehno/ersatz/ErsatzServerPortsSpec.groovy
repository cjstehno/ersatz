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
import spock.lang.AutoCleanup
import spock.lang.Specification

class ErsatzServerPortsSpec extends Specification {

    // NOTE: if this test starts failing for odd reasons, add some logic to ensure port is available

    @AutoCleanup private ErsatzServer ersatz = new ErsatzServer({
        httpPort 8675
        expectations {
            GET('/hi').responds().code(200)
        }
    })

    private HttpClient http = new HttpClient()

    void 'running with explicit port'(){
        expect:
        ersatz.start()

        http.get(ersatz.httpUrl('/hi')).code() == 200

        and:
        ersatz.httpPort == 8675
    }
}
