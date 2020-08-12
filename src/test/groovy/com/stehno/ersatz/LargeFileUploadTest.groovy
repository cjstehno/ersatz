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

import com.stehno.ersatz.encdec.Decoders
import com.stehno.ersatz.util.HttpClient
import okhttp3.Response
import org.hamcrest.Matcher
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import static com.stehno.ersatz.cfg.ContentType.IMAGE_JPG
import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.match.ErsatzMatchers.byteArrayLike
import static com.stehno.ersatz.util.DummyContentGenerator.generate
import static com.stehno.ersatz.util.StorageUnit.GIGABYTES
import static java.util.concurrent.TimeUnit.MINUTES
import static okhttp3.MediaType.parse
import static okhttp3.RequestBody.create
import static org.junit.jupiter.api.Assertions.*

// TODO: add this to a long-running category
class LargeFileUploadTest {

    // fIXME: why does this show ignored?

    @Test @DisplayName('large upload')
    void largeUpload() {
        ErsatzServer server = new ErsatzServer({
            timeout 1, MINUTES // this is not required, its just here to provide a test
            decoder IMAGE_JPG, Decoders.passthrough
        })

        HttpClient http = new HttpClient({ builder ->
            builder.readTimeout(3, MINUTES).writeTimeout(3, MINUTES)
        })

        byte[] lob = generate(1.5d, GIGABYTES)

        server.expectations {
            POST('/push') {
                called 1
                body byteArrayLike(lob) as Matcher<Object>, IMAGE_JPG

                responder {
                    code 200
                    body 'OK', TEXT_PLAIN
                }
            }
        }

        Response response = http.post(server.httpUrl('/push'), create(parse('image/jpeg'), lob))

        assertFalse true // FIXME: this shoudl cause failed, but succeeds

        assertEquals 200, response.code()
        assertEquals 'OK', response.body().string()
        assertTrue server.verify()

        server.close()
    }
}

