/*
 * Copyright (C) 2016 Christopher J. Stehno
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

import com.stehno.ersatz.feat.BasicAuthFeature
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import spock.lang.Specification

import static com.stehno.ersatz.feat.SimpleIdentityManager.encodedCredential

class BasicSpec extends Specification {

    private final ErsatzServer ersatzServer = new ErsatzServer({
        feature new BasicAuthFeature()
    })
    private final OkHttpClient client = new OkHttpClient()

    def 'BASIC auth'() {
        setup:
        ersatzServer.expectations {
            get('/secrets').responds().content('Something secure')
        }.start()

        when:
        okhttp3.Response response = client.newCall(new Builder().url("${ersatzServer.serverUrl}/secrets").build()).execute()

        then:
        response.code() == 401
        response.body().string() == ''

        when:
        response = client.newCall(
            new Builder().url("${ersatzServer.serverUrl}/secrets").addHeader('Authorization', encodedCredential('admin', '$3cr3t')).build()
        ).execute()

        then:
        response.code() == 200
        response.body().string() == 'Something secure'
    }

    def cleanup() {
        ersatzServer.stop()
    }
}
