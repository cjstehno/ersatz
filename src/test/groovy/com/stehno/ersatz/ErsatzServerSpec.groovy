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

import groovy.transform.TupleConstructor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.UploadContext
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

import static MultipartResponseContent.multipart
import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.startsWith

class ErsatzServerSpec extends Specification {

    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()
    @AutoCleanup('stop') private final ErsatzServer ersatzServer = new ErsatzServer()

    def 'prototype: functional'() {
        setup:
        ersatzServer.expectations({ expectations ->
            expectations.get('/foo').responds().content('This is Ersatz!!')
            expectations.get('/bar').responds().content('This is Bar!!')
        } as Consumer<Expectations>)

        ersatzServer.start()

        when:
        String text = "http://localhost:${ersatzServer.port}/foo".toURL().text

        then:
        text == 'This is Ersatz!!'
    }

    def 'prototype: groovy'() {
        setup:
        final AtomicInteger counter = new AtomicInteger()

        ersatzServer.expectations {
            get('/foo').called(greaterThanOrEqualTo(1)).responder {
                content 'This is Ersatz!!'
            }.responder {
                content 'This is another response'
            }

            get('/bar') {
                called greaterThanOrEqualTo(2)
                listener { req -> counter.incrementAndGet() }
                responder {
                    content 'This is Bar!!'
                }
            }

            get('/baz').query('alpha', '42').responds().content('The answer is 42')
        }

        ersatzServer.start()

        when:
        def request = new okhttp3.Request.Builder().url(url('/foo')).build()

        then:
        client.newCall(request).execute().body().string() == 'This is Ersatz!!'

        when:
        request = new okhttp3.Request.Builder().url(url('/foo')).build()

        then:
        client.newCall(request).execute().body().string() == 'This is another response'

        when:
        request = new okhttp3.Request.Builder().url(url("/bar")).build();
        def results = [
            client.newCall(request).execute().body().string(),
            client.newCall(request).execute().body().string()
        ]

        then:
        //        counter.get() == 2 TODO: this is twitchy
        results.every { it == 'This is Bar!!' }

        when:
        request = new okhttp3.Request.Builder().url(url('/baz?alpha=42')).build();

        then:
        client.newCall(request).execute().body().string() == 'The answer is 42'

        and:
        ersatzServer.verify()
    }

    def 'multipart text'() {
        setup:
        ersatzServer.expectations {
            get('/data') {
                responds().content(multipart() {
                    boundary 't8xOJjySKePdRgBHYD'
                    encoder TEXT_PLAIN.value, CharSequence, { o -> o as String }
                    field 'alpha', 'bravo'
                    part 'file', 'data.txt', TEXT_PLAIN, 'This is some file data'
                })
            }
        }.start()

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/data')).build()).execute()

        then:
        response.body().string().trim().readLines() == '''
            --t8xOJjySKePdRgBHYD
            Content-Disposition: form-data; name="alpha"
            Content-Type: text/plain
            
            bravo
            --t8xOJjySKePdRgBHYD
            Content-Disposition: form-data; name="file"; filename="data.txt"
            Content-Type: text/plain
            
            This is some file data
            --t8xOJjySKePdRgBHYD--
        '''.stripIndent().trim().readLines()
    }

    def 'multipart binary'() {
        setup:
        ersatzServer.expectations {
            get('/data') {
                responds().content(multipart() {
                    boundary 'WyAJDTEVlYgGjdI13o'
                    encoder TEXT_PLAIN, CharSequence, { o -> o as String }
                    encoder 'image/jpeg', InputStream, { o -> ((InputStream) o).bytes.encodeBase64() }
                    part 'file', 'data.txt', TEXT_PLAIN, 'This is some file data'
                    part 'image', 'test-image.jpg', 'image/jpeg', ErsatzServerSpec.getResourceAsStream('/test-image.jpg'), 'base64'
                })
            }
        }.start()

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/data')).build()).execute()

        def down = new ResponseDownloadContent(response.body())
        FileUpload fu = new FileUpload(new DiskFileItemFactory(100000, File.createTempDir()))
        List<FileItem> items = fu.parseRequest(down)

        then:
        items.size() == 2

        items[0].fieldName == 'file'
        items[0].name == 'data.txt'
        items[0].contentType == 'text/plain'
        items[0].get().length == 22

        items[1].fieldName == 'image'
        items[1].name == 'test-image.jpg'
        items[1].contentType == 'image/jpeg'

        byte[] bytes = Base64.decoder.decode(items[1].get())
        bytes.length == ErsatzServerSpec.getResourceAsStream('/test-image.jpg').bytes.length
        bytes == ErsatzServerSpec.getResourceAsStream('/test-image.jpg').bytes
    }

    def 'alternate construction'() {
        setup:
        def server = new ErsatzServer({
            expectations {
                get(startsWith('/hello')).responds().content('ok')
            }
            start()
        })

        expect:
        "${server.serverUrl}/hello/there".toURL().text == 'ok'

        cleanup:
        server.stop()
    }

    private String url(final String path) {
        "http://localhost:${ersatzServer.port}${path}"
    }

    @TupleConstructor
    private static class ResponseDownloadContent implements UploadContext {

        final ResponseBody body

        @Override
        long contentLength() {
            body.contentLength()
        }

        @Override
        String getCharacterEncoding() {
            body.contentType().charset().toString()
        }

        @Override
        String getContentType() {
            body.contentType().toString()
        }

        @Override
        int getContentLength() {
            body.contentLength()
        }

        @Override
        InputStream getInputStream() throws IOException {
            body.byteStream()
        }
    }
}

