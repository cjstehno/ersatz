/*
 * Copyright (C) 2019 Christopher J. Stehno
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
import com.stehno.ersatz.util.LongRunning
import org.hamcrest.Matcher
import org.junit.experimental.categories.Category
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.IMAGE_JPG
import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.ErsatzMatchers.byteArrayLike
import static com.stehno.ersatz.util.DummyContentGenerator.generate
import static com.stehno.ersatz.util.StorageUnit.GIGABYTES
import static com.stehno.ersatz.util.StorageUnit.MEGABYTES
import static java.util.concurrent.TimeUnit.MINUTES
import static okhttp3.MediaType.parse
import static okhttp3.RequestBody.create

// NOTE: These are separate classes so that they will run concurrently in the build

@Category(LongRunning) @Ignore("TODO: this test fails in Travis build")
class LargeFileUploadSpec extends Specification {

    private final HttpClient http = new HttpClient({ builder ->
        builder.readTimeout(3, MINUTES).writeTimeout(3, MINUTES)
    })

    @AutoCleanup
    private final ErsatzServer server = new ErsatzServer({
        timeout 1, MINUTES // this is not required, its just here to provide a test
        decoder IMAGE_JPG, Decoders.passthrough
    })

    def 'large upload'() {
        setup:
        byte[] lob = generate(1.5d, GIGABYTES)

        server.expectations {
            POST('/push') {
                called 1
                body byteArrayLike(lob) as Matcher<Object>, IMAGE_JPG

                responder {
                    code 200
                    content 'OK', TEXT_PLAIN
                }
            }
        }

        when:

        okhttp3.Response response = http.post(
            "http://localhost:${server.httpPort}${'/push'}",
            create(parse('image/jpeg'), lob)
        )

        then:
        response.code() == 200
        response.body().string() == 'OK'

        and:
        server.verify()
    }
}

@Category(LongRunning) @Ignore
class LargeFileDownloadSpec extends Specification {

    private final HttpClient http = new HttpClient({ builder ->
        builder.readTimeout(3, MINUTES).writeTimeout(3, MINUTES)
    })

    @AutoCleanup
    private final ErsatzServer server = new ErsatzServer({
        timeout 1, MINUTES // this is not required, its just here to provide a test
        encoder IMAGE_JPG, byte[].class, Encoders.binaryBase64
    })

    def 'large download'() {
        setup:
        byte[] lob = generate(500, MEGABYTES)

        server.expectations {
            GET('/download').called(1).responds().code(200).content(lob, IMAGE_JPG)
        }

        when:
        okhttp3.Response response = http.get("http://localhost:${server.httpPort}${'/download'}")

        then:
        response.code() == 200
        response.body()

        and:
        server.verify()
    }
}
