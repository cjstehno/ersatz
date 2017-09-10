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
package com.stehno.ersatz

import groovy.transform.TupleConstructor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.UploadContext
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.hamcrest.Matcher
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

import static MultipartResponseContent.multipart
import static com.stehno.ersatz.ContentType.*
import static com.stehno.ersatz.CookieMatcher.cookieMatcher
import static com.stehno.ersatz.HttpMethod.*
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.startsWith

class ErsatzServerSpec extends Specification {

    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()

    @AutoCleanup('stop') private final ErsatzServer ersatzServer = new ErsatzServer({
        encoder MULTIPART_MIXED, MultipartResponseContent, Encoders.multipart
    })

    def 'prototype: functional'() {
        setup:
        ersatzServer.expectations({ expectations ->
            expectations.get('/foo').responds().content('This is Ersatz!!')
            expectations.get('/bar').responds().content('This is Bar!!')
        } as Consumer<Expectations>)

        ersatzServer.start()

        when:
        String text = "http://localhost:${ersatzServer.httpPort}/foo".toURL().text

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
        request = new okhttp3.Request.Builder().url(url("/bar")).build()
        def results = [
            client.newCall(request).execute().body().string(),
            client.newCall(request).execute().body().string()
        ]

        then:
        counter.get() == 2
        results.every { it == 'This is Bar!!' }

        when:
        request = new okhttp3.Request.Builder().url(url('/baz?alpha=42')).build()

        then:
        client.newCall(request).execute().body().string() == 'The answer is 42'

        and:
        ersatzServer.verify()
    }

    def 'multipart text'() {
        setup:
        ersatzServer.expectations {
            get('/data') {
                responds().content(multipart {
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
                responds().content(multipart {
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
        })

        server.start()

        expect:
        "${server.httpUrl}/hello/there".toURL().text == 'ok'

        cleanup:
        server.stop()
    }

    def 'gzip compression supported'() {
        setup:
        ersatzServer.expectations {
            get('/gzip').header('Accept-Encoding', 'gzip').responds().content('x' * 1000, TEXT_PLAIN)
        }

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/gzip')).build()).execute()

        then:
        response.code() == 200
        response.networkResponse().headers('Content-Encoding').contains('gzip')
    }

    def 'non-compression supported'() {
        setup:
        ersatzServer.expectations {
            get('/gzip').header('Accept-Encoding', '').responds().content('x' * 1000, TEXT_PLAIN)
        }

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/gzip')).header('Accept-Encoding', '').build()).execute()

        then:
        response.code() == 200
        !response.networkResponse().headers('Content-Encoding').contains('gzip')
    }

    def 'deflate supported'() {
        setup:
        ersatzServer.expectations {
            get('/gzip').header('Accept-Encoding', 'deflate').responds().content('x' * 1000, TEXT_PLAIN)
        }

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/gzip')).header('Accept-Encoding', 'deflate').build()).execute()

        then:
        response.code() == 200
        response.networkResponse().headers('Content-Encoding').contains('deflate')
    }

    @Unroll 'OPTIONS #path allows #allowed'() {
        setup:
        ersatzServer.expectations {
            options('/options').responds().allows(GET, POST).code(200)
            options('/*').responds().allows(DELETE, GET, OPTIONS).code(200)
        }

        when:
        HttpURLConnection connection = new URL("${ersatzServer.httpUrl}/$path").openConnection() as HttpURLConnection
        connection.requestMethod = 'OPTIONS'

        then:
        connection.responseCode == 200
        connection.headerFields['Allow'].size() == allowed.size()
        connection.headerFields['Allow'].containsAll(allowed)
        !connection.inputStream.text

        where:
        path      || allowed
        'options' || ['GET', 'POST']
        '*'       || ['OPTIONS', 'GET', 'DELETE']
    }

    def 'TRACE sends back request'() {
        setup:
        ersatzServer.start()

        when:
        HttpURLConnection connection = new URL("${ersatzServer.httpUrl}/info?data=foo+bar").openConnection() as HttpURLConnection
        connection.requestMethod = 'TRACE'

        then:
        connection.contentType == MESSAGE_HTTP.value
        connection.inputStream.text.readLines()*.trim() == """TRACE /info?data=foo+bar HTTP/1.1
            Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
            Connection: keep-alive
            User-Agent: Java/${System.getProperty('java.version')}
            Host: localhost:${ersatzServer.httpPort}
        """.readLines()*.trim()

        connection.responseCode == 200
    }

    @Unroll 'delayed response (#delay)'() {
        setup:
        ersatzServer.expectations {
            get('/slow').responds().delay(delay).content('Done').code(200)
        }

        when:
        long started = System.currentTimeMillis()
        String response = "${ersatzServer.httpUrl}/slow".toURL().text
        long elapsed = System.currentTimeMillis() - started

        then:
        response == 'Done'
        elapsed >= time

        where:
        delay   | time
        1000    | 1000
        '1 sec' | 1000
    }

    @Unroll 'using closure as matcher (#path)'() {
        setup:
        ersatzServer.expectations {
            get({ p -> p.startsWith('/general') } as Matcher<String>).responds().content('ok').code(200)
            get('/other').responds().content('err').code(200)
        }

        expect:
        url(path).toURL().text == response

        where:
        path           || response
        '/general/one' || 'ok'
        '/general/two' || 'ok'
        '/other'       || 'err'
    }

    def 'proxied request should return proxy not original'() {
        setup:
        ErsatzServer proxyServer = new ErsatzServer({
            expectations {
                get('/proxied').called(1).responds().content('forwarded').code(200)
            }
        })

        ersatzServer.expectations {
            get('/proxied').called(0).responds().content('original').code(200)
        }

        OkHttpClient proxiedClient = new OkHttpClient.Builder()
            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', proxyServer.httpPort)))
            .cookieJar(new InMemoryCookieJar())
            .build()

        when:
        okhttp3.Response response = proxiedClient.newCall(new okhttp3.Request.Builder().get().url(url('/proxied')).build()).execute()

        then:
        response.code() == 200
        response.body().bytes() == 'forwarded'.bytes

        and:
        proxyServer.verify()
        ersatzServer.verify()
    }

    def 'multiple header matching support'() {
        setup:
        ersatzServer.expectations {
            get('/api/hello') {
                called 1
                header 'Accept', 'application/json'
                header 'Accept', 'application/vnd.company+json'
                responder {
                    code 200
                    content 'msg': 'World', 'application/vnd.company+json'
                }
            }
        }

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/api/hello'))
            .addHeader('Accept', 'application/json')
            .addHeader('Accept', 'application/vnd.company+json')
            .build()
        ).execute()

        then:
        response.code() == 200
        response.body().string() == '[msg:World]'

        and:
        ersatzServer.verify()
    }

    def 'multiple header matching support (using matcher)'() {
        setup:
        ersatzServer.expectations {
            get('/api/hello') {
                called 1
                header 'Accept', { x ->
                    'application/vnd.company+json' in x || 'application/json' in x
                } as Matcher<Iterable<String>>
                responder {
                    code 200
                    content 'msg': 'World', 'application/vnd.company+json'
                }
            }
        }

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/api/hello'))
            .addHeader('Accept', headerValue)
            .build()
        ).execute()

        then:
        response.code() == 200
        response.body().string() == '[msg:World]'

        and:
        ersatzServer.verify()

        where:
        headerValue << ['application/vnd.company+json', 'application/json']
    }

    def 'multiple header matching support (expecting two headers and had one)'() {
        setup:
        ersatzServer.expectations {
            get('/api/hello') {
                called 0
                header 'Accept', 'application/json'
                header 'Accept', 'application/vnd.company+json'
                responder {
                    code 200
                    content 'msg': 'World', 'application/vnd.company+json'
                }
            }
        }

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().get().url(url('/api/hello'))
            .addHeader('Accept', 'application/json')
            .build()
        ).execute()

        then:
        response.code() == 404

        and:
        ersatzServer.verify()
    }

    def 'baking cookies'() {
        setup:
        ersatzServer.expectations {
            get('/setkermit').called(1).responder {
                content('ok', TEXT_PLAIN)
                cookie('kermit', Cookie.cookie {
                    value 'frog'
                    path '/showkermit'
                })
            }

            get('/showkermit').cookie('kermit', cookieMatcher {
                value startsWith('frog')
            }).called(1).responder {
                content('ok', TEXT_PLAIN)
                cookie('miss', Cookie.cookie {
                    value 'piggy'
                    path '/'
                })
                cookie('fozzy', Cookie.cookie {
                    value 'bear'
                    path '/some/deep/path'
                })
            }
        }

        when:
        okhttp3.Response response = client.newCall(
            new okhttp3.Request.Builder().get().url(url('/setkermit')).build()
        ).execute()

        then:
        response.body().string() == 'ok'

        when:
        response = client.newCall(
            new okhttp3.Request.Builder().get().url(url('/showkermit')).addHeader('Cookie', 'kermit=frog; path=/showkermit').build()
        ).execute()

        then:
        response.body().string() == 'ok'

        and:
        ersatzServer.verify()
    }

    private String url(final String path) {
        "http://localhost:${ersatzServer.httpPort}${path}"
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

