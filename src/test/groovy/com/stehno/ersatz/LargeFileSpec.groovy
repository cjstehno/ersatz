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

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.hamcrest.Matcher
import org.junit.experimental.categories.Category
import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.IMAGE_JPG
import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.ErsatzMatchers.byteArrayLike
import static com.stehno.ersatz.util.DummyContentGenerator.generate
import static com.stehno.ersatz.util.StorageUnit.GIGABYTES
import static com.stehno.ersatz.util.StorageUnit.MEGABYTES
import static java.util.concurrent.TimeUnit.MINUTES

// NOTE: These are separate classes so that they will run concurrently in the build

@Category(LongRunning)
class LargeFileUploadSpec extends Specification {

    private final OkHttpClient client = new OkHttpClient.Builder()
        .cookieJar(new InMemoryCookieJar())
        .readTimeout(3, MINUTES).writeTimeout(3, MINUTES)
        .build()

    @AutoCleanup('stop')
    private final ErsatzServer server = new ErsatzServer({
        timeout 1, MINUTES // this is not required, its just here to provide a test
        decoder IMAGE_JPG, Decoders.passthrough
    })

    def 'large upload'() {
        setup:
        byte[] lob = generate(1.5d, GIGABYTES)

        server.expectations {
            post('/push') {
                called 1
                body byteArrayLike(lob) as Matcher<Object>, IMAGE_JPG

                responder {
                    code 200
                    content 'OK', TEXT_PLAIN
                }
            }
        }

        when:
        RequestBody body = RequestBody.create(MediaType.parse('image/jpeg'), lob)
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().post(body).url("http://localhost:${server.httpPort}${'/push'}").build()).execute()

        then:
        response.code() == 200
        response.body().string() == 'OK'

        and:
        server.verify()
    }
}

@Category(LongRunning)
class LargeFileDownloadSpec extends Specification {

    private final OkHttpClient client = new OkHttpClient.Builder()
        .cookieJar(new InMemoryCookieJar())
        .readTimeout(3, MINUTES).writeTimeout(3, MINUTES)
        .build()

    @AutoCleanup('stop')
    private final ErsatzServer server = new ErsatzServer({
        timeout 1, MINUTES // this is not required, its just here to provide a test
        encoder IMAGE_JPG, byte[].class, Encoders.binaryBase64
    })

    def 'large download'() {
        setup:
        byte[] lob = generate(500, MEGABYTES)

        server.expectations {
            get('/download').called(1).responds().code(200).content(lob, IMAGE_JPG)
        }

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url("http://localhost:${server.httpPort}${'/download'}").build()).execute()

        then:
        response.code() == 200
        response.body()

        and:
        server.verify()
    }
}