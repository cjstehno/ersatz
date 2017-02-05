/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.junit

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.Rule
import org.junit.Test

import static java.lang.String.format
import static org.junit.Assert.assertEquals

class ErsatzServerRuleGroovyTest {

    @Rule public ErsatzServerRule ersatzServer = new ErsatzServerRule({
        expects().get('/testing').responds().content('ok')
    })

    @Test void testing() throws IOException {
        ersatzServer.start()

        Response response = new OkHttpClient().newCall(
            new Request.Builder().url(format("%s/testing", ersatzServer.getHttpUrl())).build()
        ).execute()

        assertEquals(200, response.code())
        assertEquals("ok", response.body().string())
    }
}
