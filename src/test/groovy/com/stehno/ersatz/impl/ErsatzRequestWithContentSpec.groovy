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
package com.stehno.ersatz.impl

import com.stehno.ersatz.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.*
import static com.stehno.ersatz.ErsatzServer.NOT_FOUND_BODY
import static com.stehno.ersatz.MultipartRequestMatcher.multipartMatcher
import static okhttp3.MediaType.parse
import static okhttp3.Request.Builder
import static okhttp3.RequestBody.create
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue

class ErsatzRequestWithContentSpec extends Specification {

    private static final String BODY_CONTENT = '{"label":"Body Content", "text":"This is some body content."}'
    private OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()
    private ErsatzRequestWithContent request = new ErsatzRequestWithContent(HttpMethod.POST, equalTo('/posting'))
    @AutoCleanup('stop') private ErsatzServer server = new ErsatzServer()

    def 'body with content-type'() {
        when:
        request.body(BODY_CONTENT, TEXT_PLAIN).decoder(TEXT_PLAIN, Decoders.utf8String)

        then:
        request.matches(new MockClientRequest(method: HttpMethod.POST, path: '/posting', body: BODY_CONTENT.bytes).header('Content-Type', TEXT_PLAIN.value))
    }

    def 'to string'() {
        setup:
        request.body('Some body', TEXT_PLAIN)

        expect:
        request.toString() == 'Expectations (ErsatzRequestWithContent): <POST>, "/posting", a collection containing a string starting with "text/plain", "Some body", '
    }

    def 'matching: body'() {
        setup:
        server.expectations {
            POST('/posting') {
                body BODY_CONTENT, TEXT_PLAIN
                decoder TEXT_PLAIN, Decoders.utf8String
                responder {
                    body 'accepted', TEXT_PLAIN
                }
            }
        }.start()

        when:
        String value = exec(clientPost('/posting', 'text/plain', BODY_CONTENT).build()).body().string()

        then:
        value == 'accepted'

        when:
        value = exec(clientPost('/posting', 'text/plain', '').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: body and content-type'() {
        setup:
        server.expectations {
            post('/posting').body(BODY_CONTENT, 'text/plain; charset=utf-8').decoder(TEXT_PLAIN, Decoders.utf8String).responds().body('accepted')
        }.start()

        when:
        String value = exec(clientPost('/posting', 'text/plain; charset=utf-8', BODY_CONTENT).build()).body().string()

        then:
        value == 'accepted'

        when:
        value = exec(clientPost('/posting', 'text/html', BODY_CONTENT).build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: body with converter (builder)'() {
        setup:
        server.expectations {
            post('/posting').body([label: "Body Content", text: "This is some body content."], 'some/json; charset=utf-8')
                    .decoder('some/json; charset=utf-8', Decoders.parseJson)
                    .responds().body('accepted')
        }.start()

        when:
        String value = exec(clientPost('/posting', 'some/json; charset=utf-8', BODY_CONTENT).build()).body().string()

        then:
        value == 'accepted'

        when:
        value = exec(clientPost('/posting', 'text/html', BODY_CONTENT).build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: body with converter (dls)'() {
        setup:
        String responseContent = 'accepted'

        server.expectations {
            post('/posting') {
                body([label: "Body Content", text: "This is some body content."], 'some/json; charset=utf-8')
                decoder(new ContentType('some/json; charset=utf-8'), Decoders.parseJson)
                responder {
                    body responseContent
                }
            }
        }.start()

        when:
        String value = exec(clientPost('/posting', 'some/json; charset=utf-8', BODY_CONTENT).build()).body().string()

        then:
        value == responseContent

        when:
        value = exec(clientPost('/posting', 'text/html', BODY_CONTENT).build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'application/x-www-form-urlencoded'() {
        setup:
        server.expectations {
            post('/form') {
                decoder APPLICATION_URLENCODED, Decoders.urlEncoded
                body([alpha: 'some data', bravo: '42', charlie: 'last'], 'application/x-www-form-urlencoded; charset=utf-8')
                responder {
                    body 'ok'
                }
            }
        }.start()

        when:
        OkHttpClient client = new OkHttpClient()

        Builder builder = new Builder().post(create(parse('application/x-www-form-urlencoded'), 'alpha=some+data&bravo=42&charlie=last'))
                .url("${server.httpUrl}/form")
                .addHeader('Content-Type', 'application/x-www-form-urlencoded')

        Response response = client.newCall(builder.build()).execute()

        then:
        response.body().string() == 'ok'
    }

    def 'multipart/form-data'() {
        setup:
        server.expectations {
            post('/upload') {
                decoder TEXT_PLAIN, Decoders.utf8String
                decoder MULTIPART_MIXED, Decoders.multipart
                decoder IMAGE_PNG, Decoders.passthrough
                body MultipartRequestContent.multipart {
                    part 'something', TEXT_PLAIN, 'interesting'
                    part 'infoFile', 'info.txt', 'text/plain; charset=utf-8', 'This is some interesting file content.'
                    part 'dataFile', 'data.bin', IMAGE_PNG, [8, 6, 7, 5, 3, 0, 9] as byte[]
                }, MULTIPART_MIXED
                responder {
                    body 'ok'
                }
            }
        }.start()

        OkHttpClient client = new OkHttpClient()

        when:
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .addFormDataPart('something', 'interesting')
                .addFormDataPart('infoFile', 'info.txt', create(parse('text/plain'), 'This is some interesting file content.'))
                .addFormDataPart('dataFile', 'data.bin', create(parse('image/png'), [8, 6, 7, 5, 3, 0, 9] as byte[]))

        Builder builder = new Builder().post(bodyBuilder.build()).url("${server.httpUrl}/upload").addHeader('Content-Type', 'multipart/form-data')

        Response response = client.newCall(builder.build()).execute()

        then:
        response.body().string() == 'ok'
    }

    def 'multipart/form-data using matcher object'() {
        setup:
        server.expectations {
            post('/upload') {
                decoder TEXT_PLAIN, Decoders.utf8String
                decoder MULTIPART_MIXED, Decoders.multipart
                decoder IMAGE_PNG, Decoders.passthrough
                body multipartMatcher {
                    part 'something', 'interesting'
                    part 'infoFile', 'info.txt', 'text/plain', equalTo('This is some interesting file content.')
                    part 'dataFile', 'data.bin', IMAGE_PNG, notNullValue()
                }, MULTIPART_MIXED
                responder {
                    body 'ok'
                }
            }
        }.start()

        OkHttpClient client = new OkHttpClient()

        when:
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .addFormDataPart('something', 'interesting')
                .addFormDataPart('infoFile', 'info.txt', create(parse('text/plain'), 'This is some interesting file content.'))
                .addFormDataPart('dataFile', 'data.bin', create(parse('image/png'), [8, 6, 7, 5, 3, 0, 9] as byte[]))

        Builder builder = new Builder().post(bodyBuilder.build()).url("${server.httpUrl}/upload").addHeader('Content-Type', 'multipart/form-data')

        Response response = client.newCall(builder.build()).execute()

        then:
        response.body().string() == 'ok'
    }

    private Builder clientPost(final String path, final String contentType, final String content) {
        new Builder().post(create(parse(contentType), content)).url("${server.httpUrl}${path}")
    }

    private Response exec(Request req) {
        client.newCall(req).execute()
    }
}