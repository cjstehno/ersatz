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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.InMemoryCookieJar
import groovy.json.JsonSlurper
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.ErsatzServer.NOT_FOUND_BODY
import static com.stehno.ersatz.MultipartContentMatcher.attrs
import static com.stehno.ersatz.MultipartContentMatcher.multipart
import static okhttp3.MediaType.parse
import static okhttp3.Request.Builder
import static okhttp3.RequestBody.create

class ErsatzRequestWithContentSpec extends Specification {

    private static final String BODY_CONTENT = '{"label":"Body Content", "text":"This is some body content."}'
    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()
    private final ErsatzRequestWithContent request = new ErsatzRequestWithContent('POST', '/posting')
    @AutoCleanup('stop') private final ErsatzServer server = new ErsatzServer()

    def 'body'() {
        when:
        request.body(BODY_CONTENT)

        then:
        request.body == BODY_CONTENT
    }

    def 'contentType'() {
        when:
        request.contentType('image/jpeg')

        then:
        request.contentType == 'image/jpeg'
    }

    def 'body with content-type'() {
        when:
        request.body(BODY_CONTENT, TEXT_PLAIN)

        then:
        request.body == BODY_CONTENT
        request.contentType == 'text/plain'
    }

    def 'to string'() {
        setup:
        request.body('Some body')

        expect:
        request.toString() == '{ POST /posting (query=[:], headers=[:], cookies=[:]): counted=0 }: Some body'
    }

    def 'matching: body'() {
        setup:
        server.expectations {
            post('/posting').body(BODY_CONTENT).responds().content('accepted')
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
            post('/posting').body(BODY_CONTENT, 'text/plain; charset=utf-8').responds().content('accepted')
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
            post('/posting').body([label: "Body Content", text: "This is some body content."]).contentType('some/json; charset=utf-8').converter('some/json; charset=utf-8', { b ->
                new JsonSlurper().parse(b)
            }).responds().content('accepted')
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
                body([label: "Body Content", text: "This is some body content."])
                contentType 'some/json; charset=utf-8'
                converter(new ContentType('some/json; charset=utf-8'), { b -> new JsonSlurper().parse b })
                responder {
                    content responseContent
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
                body([alpha: 'some data', bravo: '42', charlie: 'last'], 'application/x-www-form-urlencoded; charset=utf-8')
                responder {
                    content 'ok'
                }
            }
        }.start()

        when:
        OkHttpClient client = new OkHttpClient()

        Builder builder = new Builder().post(create(parse('application/x-www-form-urlencoded'), 'alpha=some+data&bravo=42&charlie=last'))
            .url("${server.serverUrl}/form")
            .addHeader('Content-Type', 'application/x-www-form-urlencoded')

        Response response = client.newCall(builder.build()).execute()

        then:
        response.body().string() == 'ok'
    }

    def 'multipart/form-data'() {
        setup:
        server.expectations {
            post('/upload') {
                condition { cr ->
                    attrs(cr.fileItems[0], fieldName: 'something', string: 'interesting') &&
                        attrs(cr.fileItems[1], fieldName: 'infoFile', string: 'This is some interesting file content.') &&
                        attrs(cr.fileItems[2], fieldName: 'dataFile', size: 7)
                }
                responder {
                    content 'ok'
                }
            }
        }.start()

        OkHttpClient client = new OkHttpClient()

        when:
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
            .addFormDataPart('something', 'interesting')
            .addFormDataPart('infoFile', 'info.txt', create(parse('text/plain'), 'This is some interesting file content.'))
            .addFormDataPart('dataFile', 'data.bin', create(parse('image/png'), [8, 6, 7, 5, 3, 0, 9] as byte[]))

        Builder builder = new Builder().post(bodyBuilder.build()).url("${server.serverUrl}/upload").addHeader('Content-Type', 'multipart/form-data')

        Response response = client.newCall(builder.build()).execute()

        then:
        response.body().string() == 'ok'
    }

    def 'multipart/form-data using matcher object'() {
        setup:
        server.expectations {
            post('/upload') {
                condition multipart {
                    field(0, 'something', 'interesting') &&
                        file(1, 'infoFile', 'info.txt', 'text/plain', 'This is some interesting file content.') &&
                        part(2, fieldName: 'dataFile', size: 7, fileName: 'data.bin', contentType: 'image/png', bytes: [8, 6, 7, 5, 3, 0, 9] as byte[])
                }
                responder {
                    content 'ok'
                }
            }
        }.start()

        OkHttpClient client = new OkHttpClient()

        when:
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
            .addFormDataPart('something', 'interesting')
            .addFormDataPart('infoFile', 'info.txt', create(parse('text/plain'), 'This is some interesting file content.'))
            .addFormDataPart('dataFile', 'data.bin', create(parse('image/png'), [8, 6, 7, 5, 3, 0, 9] as byte[]))

        Builder builder = new Builder().post(bodyBuilder.build()).url("${server.serverUrl}/upload").addHeader('Content-Type', 'multipart/form-data')

        Response response = client.newCall(builder.build()).execute()

        then:
        response.body().string() == 'ok'
    }

    private Builder clientPost(final String path, final String contentType, final String content) {
        new Builder().post(create(parse(contentType), content)).url("${server.serverUrl}${path}")
    }

    private Response exec(Request req) {
        client.newCall(req).execute()
    }
}