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

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import okhttp3.RequestBody
import spock.lang.AutoCleanup
import spock.lang.Specification

class UploadSpec extends Specification {

    // TODO: Content-Type: application/x-www-form-urlencoded
    // TODO: file upload (multipart, multifile) multipart/form-data
    // FIXME: move these to proper test classes

    @AutoCleanup('stop') private final ErsatzServer ersatzServer = new ErsatzServer()

    def 'application/x-www-form-urlencoded'() {
        setup:
        ersatzServer.expectations {
            post('/upload') {
                body([alpha: 'some data', bravo: '42', charlie: 'last'], 'application/x-www-form-urlencoded; charset=utf-8')
                responder {
                    content 'ok'
                }
            }
        }.start()

        String formData = 'alpha=some+data&bravo=42&charlie=last'

        when:
        OkHttpClient client = new OkHttpClient()

        Builder builder = new Builder().post(RequestBody.create(MediaType.parse('application/x-www-form-urlencoded'), formData))
            .url("${ersatzServer.serverUrl}/upload")
            .addHeader('Content-Type', 'application/x-www-form-urlencoded')

        okhttp3.Response response = client.newCall(builder.build()).execute()

        then:
        response.body().string() == 'ok'
    }
}
