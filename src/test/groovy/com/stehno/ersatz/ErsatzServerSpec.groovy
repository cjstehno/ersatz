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
import groovy.transform.TupleConstructor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.UploadContext
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.hamcrest.Matcher
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

import static MultipartResponseContent.multipart
import static com.stehno.ersatz.ContentType.*
import static com.stehno.ersatz.CookieMatcher.cookieMatcher
import static okhttp3.MediaType.parse
import static okhttp3.RequestBody.create
import static org.awaitility.Awaitility.await
import static org.hamcrest.Matchers.*

class ErsatzServerSpec extends Specification {

    private HttpClient http = new HttpClient()

    @AutoCleanup private ErsatzServer ersatzServer = new ErsatzServer({
        encoder MULTIPART_MIXED, MultipartResponseContent, Encoders.multipart
    })

    def 'prototype: functional'() {
        setup:
        ersatzServer.expectations({ expectations ->
            expectations.GET('/foo').responds().body('This is Ersatz!!')
            expectations.GET('/bar').responds().body('This is Bar!!')
        } as Consumer<Expectations>)

        expect:
        "http://localhost:${ersatzServer.httpPort}/foo".toURL().text == 'This is Ersatz!!'
    }

    def 'prototype: groovy'() {
        setup:
        final AtomicInteger counter = new AtomicInteger()

        ersatzServer.expectations {
            GET('/foo').called(greaterThanOrEqualTo(1)).responder {
                body 'This is Ersatz!!'
            }.responder {
                body 'This is another response'
            }

            GET('/bar') {
                called greaterThanOrEqualTo(2)
                listener { req -> counter.incrementAndGet() }
                responder {
                    body 'This is Bar!!'
                }
            }

            GET('/baz').query('alpha', '42').responds().body('The answer is 42')
        }

        ersatzServer.start()

        when:
        def response = http.get(ersatzServer.httpUrl('/foo'))

        then:
        response.body().string() == 'This is Ersatz!!'

        when:
        response = http.get(ersatzServer.httpUrl('/foo'))

        then:
        response.body().string() == 'This is another response'

        when:
        def results = [
                http.get(ersatzServer.httpUrl('/bar')).body().string(),
                http.get(ersatzServer.httpUrl('/bar')).body().string()
        ]

        await().untilAtomic(counter, equalTo(2))

        then:
        results.every { it == 'This is Bar!!' }

        when:
        response = http.get(ersatzServer.httpUrl('/baz?alpha=42'))

        then:
        response.body().string() == 'The answer is 42'

        and:
        ersatzServer.verify()
    }

    @Unroll 'chunked response: #chunkDelay'() {
        setup:
        ersatzServer.timeout(1, TimeUnit.MINUTES)
        ersatzServer.expectations {
            GET('/chunky').responder {
                body 'This is chunked content', TEXT_PLAIN
                chunked {
                    chunks 3
                    delay chunkDelay
                }
            }
        }

        when:
        def response = http.get(ersatzServer.httpUrl('/chunky'))

        then:
        response.header('Transfer-encoding') == 'chunked'
        response.body().string() == 'This is chunked content'

        where:
        label   | chunkDelay
        'range' | 10..25
        'fixed' | 15
    }

    def 'valueless query string param'() {
        setup:
        ersatzServer.expectations {
            GET('/something').query('enabled').responds().code(200).body('OK', TEXT_PLAIN)
        }

        when:
        def response = http.get(ersatzServer.httpUrl('/something?enabled'))

        then:
        response.code() == 200
        response.body().string() == 'OK'

        and:
        ersatzServer.verify()
    }

    def 'multipart text'() {
        setup:
        ersatzServer.expectations {
            GET('/data') {
                responds().body(multipart {
                    boundary 't8xOJjySKePdRgBHYD'
                    encoder TEXT_PLAIN.value, CharSequence, { o -> o as String }
                    field 'alpha', 'bravo'
                    part 'file', 'data.txt', TEXT_PLAIN, 'This is some file data'
                })
            }
        }.start()

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/data'))

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
            GET('/data') {
                responds().body(multipart {
                    boundary 'WyAJDTEVlYgGjdI13o'
                    encoder TEXT_PLAIN, CharSequence, { o -> o as String }
                    encoder 'image/jpeg', InputStream, { o -> ((InputStream) o).bytes.encodeBase64().toString() }
                    part 'file', 'data.txt', TEXT_PLAIN, 'This is some file data'
                    part 'image', 'test-image.jpg', 'image/jpeg', ErsatzServerSpec.getResourceAsStream('/test-image.jpg'), 'base64'
                })
            }
        }.start()

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/data'))

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
                GET(startsWith('/hello')).responds().body('ok')
            }
        }).start()

        expect:
        "${server.httpUrl}/hello/there".toURL().text == 'ok'

        cleanup:
        server.stop()
    }

    def 'gzip compression supported'() {
        setup:
        ersatzServer.expectations {
            GET('/gzip').header('Accept-Encoding', 'gzip').responds().body('x' * 1000, TEXT_PLAIN)
        }

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/gzip'))

        then:
        response.code() == 200
        response.networkResponse().headers('Content-Encoding').contains('gzip')
    }

    def 'non-compression supported'() {
        setup:
        ersatzServer.expectations {
            GET('/gzip').header('Accept-Encoding', '').responds().body('x' * 1000, TEXT_PLAIN)
        }

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/gzip'), 'Accept-Encoding': '')

        then:
        response.code() == 200
        !response.networkResponse().headers('Content-Encoding').contains('gzip')
    }

    def 'deflate supported'() {
        setup:
        ersatzServer.expectations {
            GET('/gzip').header('Accept-Encoding', 'deflate').responds().body('x' * 1000, TEXT_PLAIN)
        }

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/gzip'), 'Accept-Encoding': 'deflate')

        then:
        response.code() == 200
        response.networkResponse().headers('Content-Encoding').contains('deflate')
    }

    @Unroll 'OPTIONS #path allows #allowed'() {
        setup:
        ersatzServer.expectations {
            OPTIONS('/options').responds().allows(HttpMethod.GET, HttpMethod.POST).code(200)
            OPTIONS('/*').responds().allows(HttpMethod.DELETE, HttpMethod.GET, HttpMethod.OPTIONS).code(200)
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
            GET('/slow').responds().delay(delay).body('Done').code(200)
        }

        when:
        long started = System.currentTimeMillis()
        String response = "${ersatzServer.httpUrl}/slow".toURL().text
        long elapsed = System.currentTimeMillis() - started

        then:
        response == 'Done'
        elapsed >= (time - 10) // there is some wiggle room

        where:
        delay  | time
        1000   | 1000
        'PT1S' | 1000
    }

    @Unroll 'using closure as matcher (#path)'() {
        setup:
        ersatzServer.expectations {
            GET({ p -> p.startsWith('/general') } as Matcher<String>).responds().body('ok').code(200)
            GET('/other').responds().body('err').code(200)
        }

        expect:
        ersatzServer.httpUrl(path).toURL().text == resp

        where:
        path           || resp
        '/general/one' || 'ok'
        '/general/two' || 'ok'
        '/other'       || 'err'
    }

    def 'proxied request should return proxy not original'() {
        setup:
        ErsatzServer proxyServer = new ErsatzServer({
            expectations {
                GET('/proxied').called(1).responds().body('forwarded').code(200)
            }
        }).start()

        ersatzServer.expectations {
            GET('/proxied').called(0).responds().body('original').code(200)
        }

        OkHttpClient proxiedClient = new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', proxyServer.httpPort)))
                .cookieJar(new InMemoryCookieJar())
                .build()

        when:
        okhttp3.Response response = proxiedClient.newCall(new okhttp3.Request.Builder().get().url(ersatzServer.httpUrl('/proxied')).build()).execute()

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
            GET('/api/hello') {
                called 1
                header 'Accept', 'application/json'
                header 'Accept', 'application/vnd.company+json'
                responder {
                    code 200
                    body 'msg': 'World', 'application/vnd.company+json'
                }
            }
        }

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/api/hello'), 'Accept': ['application/json', 'application/vnd.company+json'])

        then:
        response.code() == 200
        response.body().string() == '{msg=World}'

        and:
        ersatzServer.verify()
    }

    def 'multiple header matching support (using matcher)'() {
        setup:
        ersatzServer.expectations {
            GET('/api/hello') {
                called 1
                header 'Accept', { x ->
                    'application/vnd.company+json' in x || 'application/json' in x
                } as Matcher<Iterable<String>>
                responder {
                    code 200
                    body 'msg': 'World', 'application/vnd.company+json'
                }
            }
        }

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/api/hello'), 'Accept': headerValue)

        then:
        response.code() == 200
        response.body().string() == '{msg=World}'

        and:
        ersatzServer.verify()

        where:
        headerValue << ['application/vnd.company+json', 'application/json']
    }

    def 'multiple header matching support (expecting two headers and had one)'() {
        setup:
        ersatzServer.expectations {
            GET('/api/hello') {
                called 0
                header 'Accept', 'application/json'
                header 'Accept', 'application/vnd.company+json'
                responder {
                    code 200
                    body 'msg': 'World', 'application/vnd.company+json'
                }
            }
        }

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/api/hello'), 'Accept': 'application/json')

        then:
        response.code() == 404

        and:
        ersatzServer.verify()
    }

    def 'baking cookies'() {
        setup:
        ersatzServer.expectations {
            GET('/setkermit').called(1).responder {
                body('ok', TEXT_PLAIN)
                cookie('kermit', Cookie.cookie {
                    value 'frog'
                    path '/showkermit'
                })
            }

            GET('/showkermit').cookie('kermit', cookieMatcher {
                value startsWith('frog')
            }).called(1).responder {
                body('ok', TEXT_PLAIN)
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
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/setkermit'))

        then:
        response.body().string() == 'ok'

        when:
        response = http.get(ersatzServer.httpUrl('/showkermit'), 'Cookie': 'kermit=frog; path=/showkermit')

        then:
        response.body().string() == 'ok'

        and:
        ersatzServer.verify()
    }

    def 'variable-case headers'() {
        setup:
        ersatzServer.expectations {
            POST('*') {
                body({ true } as Matcher<Object>, APPLICATION_URLENCODED)
                header('Something-Headery', 'a-value')
                responds().body('OK')
            }
        }

        when:
        okhttp3.Response response = http.post(
                ersatzServer.httpUrl('/postit'),
                create(parse(APPLICATION_URLENCODED.value), 'Posted'),
                'something-headery': 'a-value'
        )

        then:
        response.body().string() == 'OK'
    }

    def 'post params'() {
        setup:
        ersatzServer.expectations {
            POST('/updates') {
                param('foo', 'bar')
                responds().code(201)
            }
        }

        when:
        okhttp3.Response response = http.post(
                ersatzServer.httpUrl('/updates'),
                create(parse(APPLICATION_URLENCODED.value), 'foo=bar')
        )

        then:
        response.code() == 201
    }

    void 'Proper closure delegation'() {
        setup:
        ersatzServer.expectations {
            GET("/headers") {
                header('Accept', 'application/json')

                responder {
                    header("Bad", "code")
                    body('{"hello":"world"}', 'application/json')
                }
            }
        }

        when:
        okhttp3.Response response = http.get(ersatzServer.httpUrl('/headers'), Accept: 'application/json')

        then:
        response.body().string() == '{"hello":"world"}'
        response.header('Bad') == 'code'
    }

    void 'Proper delegation of content body request/response'() {
        setup:
        ersatzServer.expectations {
            POST('/booga') {
                decoder TEXT_PLAIN, Decoders.utf8String
                body 'a request', TEXT_PLAIN
                responder {
                    body 'a response', TEXT_PLAIN
                }
            }
        }

        when:
        def response = http.post(
                ersatzServer.httpUrl('/booga'),
                create(parse('text/plain'), 'a request')
        )

        then:
        response.body().string() == 'a response'
    }

    void 'Multiple responses for GET request'() {
        setup:
        ersatzServer.expectations {
            GET('/aclue') {
                header 'Info', 'value'
                responder {
                    code 200
                    body 'Alpha', TEXT_PLAIN
                }
                responder {
                    code 200
                    body 'Bravo', TEXT_PLAIN
                }
            }
        }

        when:
        def response = http.get(ersatzServer.httpUrl('/aclue'), Info: 'value')

        then:
        response.code() == 200
        response.body().string() == 'Alpha'

        when:
        response = http.get(ersatzServer.httpUrl('/aclue'), Info: 'value')

        then:
        response.code() == 200
        response.body().string() == 'Bravo'
    }

    void 'Multiple responses for PUT request'() {
        setup:
        ersatzServer.expectations {
            PUT('/aclue') {
                header 'Info', 'value'
                responder {
                    code 200
                }
                responder {
                    code 200
                    body 'Bravo', TEXT_PLAIN
                }
            }
        }

        def payload = create(parse('text/plain'), 'payload')

        when:
        def response = http.put(ersatzServer.httpUrl('/aclue'), payload, Info: 'value')

        then:
        response.code() == 200
        response.body().string() == ''

        when:
        response = http.put(ersatzServer.httpUrl('/aclue'), payload, Info: 'value')

        then:
        response.code() == 200
        response.body().string() == 'Bravo'
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

