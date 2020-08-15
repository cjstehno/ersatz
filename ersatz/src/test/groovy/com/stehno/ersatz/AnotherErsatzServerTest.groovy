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

import com.stehno.ersatz.cfg.Expectations
import com.stehno.ersatz.cfg.HttpMethod
import com.stehno.ersatz.encdec.*
import com.stehno.ersatz.junit.ErsatzServerExtension
import com.stehno.ersatz.util.HttpClient
import groovy.transform.TupleConstructor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.UploadContext
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.hamcrest.Matcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.stream.Stream

import static com.stehno.ersatz.cfg.ContentType.*
import static com.stehno.ersatz.encdec.MultipartResponseContent.multipartResponse
import static com.stehno.ersatz.match.CookieMatcher.cookieMatcher
import static java.lang.System.currentTimeMillis
import static java.util.concurrent.TimeUnit.MINUTES
import static okhttp3.MediaType.parse
import static okhttp3.RequestBody.create
import static org.awaitility.Awaitility.await
import static org.hamcrest.Matchers.*
import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.params.provider.Arguments.arguments

@ExtendWith(ErsatzServerExtension)
class AnotherErsatzServerTest {

    // FIXME: see how much is the same between ErsatzServerTest and this class (reduce duplication)

    private ErsatzServer ersatzServer = new ErsatzServer({
        encoder MULTIPART_MIXED, MultipartResponseContent, Encoders.multipart
    })
    private HttpClient http

    @BeforeEach void beforeEach() {
        http = new HttpClient()
    }

    @Test void 'prototype: functional'() {
        ersatzServer.expectations({ expectations ->
            expectations.GET('/foo').responds().body('This is Ersatz!!')
            expectations.GET('/bar').responds().body('This is Bar!!')
        } as Consumer<Expectations>)

        assertEquals 'This is Ersatz!!', "http://localhost:${ersatzServer.httpPort}/foo".toURL().text
    }

    @Test void 'prototype: groovy'() {
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

        def response = http.get(ersatzServer.httpUrl('/foo'))
        assertEquals 'This is Ersatz!!', response.body().string()

        response = http.get(ersatzServer.httpUrl('/foo'))
        assertEquals 'This is another response', response.body().string()

        def results = [
            http.get(ersatzServer.httpUrl('/bar')).body().string(),
            http.get(ersatzServer.httpUrl('/bar')).body().string()
        ]

        await().untilAtomic(counter, equalTo(2))

        assertTrue results.every { it == 'This is Bar!!' }

        response = http.get(ersatzServer.httpUrl('/baz?alpha=42'))

        assertEquals 'The answer is 42', response.body().string()

        assertTrue ersatzServer.verify()
    }

    @ParameterizedTest @DisplayName('chunked response: #chunkDelay') @MethodSource('chunkDelayProvider')
    void chunkedResponseWithDelay(String label, chunkDelay) {
        ersatzServer.timeout(1, MINUTES)
        ersatzServer.expectations {
            GET('/chunky').responder {
                body 'This is chunked content', TEXT_PLAIN
                chunked {
                    chunks 3
                    delay chunkDelay
                }
            }
        }

        def response = http.get(ersatzServer.httpUrl('/chunky'))

        assertEquals 'chunked', response.header('Transfer-encoding')
        assertEquals 'This is chunked content', response.body().string()
    }

    private static Stream<Arguments> chunkDelayProvider() {
        Stream.of(
            arguments('range', 10..25),
            arguments('fixed', 15)
        )
    }

    @Test void 'valueless query string param'() {
        ersatzServer.expectations {
            GET('/something').query('enabled').responds().code(200).body('OK', TEXT_PLAIN)
        }

        def response = http.get(ersatzServer.httpUrl('/something?enabled'))

        assertEquals 200, response.code()
        assertEquals 'OK', response.body().string()
        assertTrue ersatzServer.verify()
    }

    @Test void 'multipart text'() {
        ersatzServer.expectations {
            GET('/data') {
                responder {
                    encoder MULTIPART_MIXED, ErsatzMultipartResponseContent, Encoders.multipart
                    body multipartResponse {
                        boundary 't8xOJjySKePdRgBHYD'
                        encoder TEXT_PLAIN.value, CharSequence, { o -> (o as String).bytes }
                        field 'alpha', 'bravo'
                        part 'file', 'data.txt', TEXT_PLAIN, 'This is some file data'
                    }
                }
            }
        }

        Response response = http.get(ersatzServer.httpUrl('/data'))

        def expectedLines = '''
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

        def actualLines = response.body().string().trim().readLines()

        assertEquals expectedLines.size(), actualLines.size()

        expectedLines.eachWithIndex { li, idx ->
            assertEquals li.trim(), actualLines[idx].trim()
        }
    }

    @Test void 'multipart binary'() {
        ersatzServer.expectations {
            GET('/data') {
                responder {
                    encoder MULTIPART_MIXED, ErsatzMultipartResponseContent, Encoders.multipart
                    body multipartResponse {
                        boundary 'WyAJDTEVlYgGjdI13o'
                        encoder TEXT_PLAIN, CharSequence, Encoders.text
                        encoder 'image/jpeg', InputStream, Encoders.inputStream
                        part 'file', 'data.txt', TEXT_PLAIN, 'This is some file data'
                        part 'image', 'test-image.jpg', 'image/jpeg', AnotherErsatzServerTest.getResourceAsStream('/test-image.jpg'), 'base64'
                    }
                }
            }
        }

        Response response = http.get(ersatzServer.httpUrl('/data'))

        def down = new ResponseDownloadContent(response.body())
        FileUpload fu = new FileUpload(new DiskFileItemFactory(100000, File.createTempDir()))
        List<FileItem> items = fu.parseRequest(down)

        assertEquals 2, items.size()

        assertEquals 'file', items[0].fieldName
        assertEquals 'data.txt', items[0].name
        assertEquals 'text/plain', items[0].contentType
        assertEquals 22, items[0].get().length

        assertEquals 'image', items[1].fieldName
        assertEquals 'test-image.jpg', items[1].name
        assertEquals 'image/jpeg', items[1].contentType
        assertEquals AnotherErsatzServerTest.getResourceAsStream('/test-image.jpg').bytes.length, items[1].size
        assertArrayEquals AnotherErsatzServerTest.getResourceAsStream('/test-image.jpg').bytes, items[1].get()
    }

    @Test @DisplayName('multipart binary (simpler)')
    void multipartBinarySimpler() {
        ersatzServer.expectations {
            GET('/stuff') {
                responder {
                    encoder MULTIPART_MIXED, ErsatzMultipartResponseContent, Encoders.multipart
                    body(multipartResponse {
                        boundary 'WyAJDTEVlYgGjdI13o'
                        encoder IMAGE_JPG, InputStream, Encoders.inputStream
                        part 'image', 'test-image.jpg', IMAGE_JPG, AnotherErsatzServerTest.getResourceAsStream('/test-image.jpg'), 'base64'
                    })
                }
            }
        }

        Response response = http.get(ersatzServer.httpUrl('/stuff'))

        def down = new ResponseDownloadContent(response.body())
        FileUpload fu = new FileUpload(new DiskFileItemFactory(100000, File.createTempDir()))
        List<FileItem> items = fu.parseRequest(down)

        assertEquals 1, items.size()

        assertEquals 'image', items[0].fieldName
        assertEquals 'test-image.jpg', items[0].name
        assertEquals 'image/jpeg', items[0].contentType
        assertEquals AnotherErsatzServerTest.getResourceAsStream('/test-image.jpg').bytes.length, items[0].size
        assertArrayEquals AnotherErsatzServerTest.getResourceAsStream('/test-image.jpg').bytes, items[0].get()
    }

    @Test void 'alternate construction'() {
        def server = new ErsatzServer({
            expectations {
                GET(startsWith('/hello')).responds().body('ok')
            }
        })

        assertEquals 'ok', "${server.httpUrl}/hello/there".toURL().text

        server.stop()
    }

    @Test void 'gzip compression supported'() {
        ersatzServer.expectations {
            GET('/gzip').header('Accept-Encoding', 'gzip').responds().body('x' * 1000, TEXT_PLAIN)
        }

        Response response = http.get(ersatzServer.httpUrl('/gzip'))

        assertEquals 200, response.code()
        assertTrue response.networkResponse().headers('Content-Encoding').contains('gzip')
    }

    @Test void 'non-compression supported'() {
        ersatzServer.expectations {
            GET('/gzip').header('Accept-Encoding', '').responds().body('x' * 1000, TEXT_PLAIN)
        }

        Response response = http.get(ersatzServer.httpUrl('/gzip'), 'Accept-Encoding': '')

        assertEquals 200, response.code()
        assertFalse response.networkResponse().headers('Content-Encoding').contains('gzip')
    }

    @Test void 'deflate supported'() {
        ersatzServer.expectations {
            GET('/gzip').header('Accept-Encoding', 'deflate').responds().body('x' * 1000, TEXT_PLAIN)
        }

        Response response = http.get(ersatzServer.httpUrl('/gzip'), 'Accept-Encoding': 'deflate')

        assertEquals 200, response.code()
        assertTrue response.networkResponse().headers('Content-Encoding').contains('deflate')
    }

    @ParameterizedTest @DisplayName('OPTIONS #path allows #allowed') @MethodSource('optionsProvider')
    void optionsPathAllows(String path, Collection<String> allowed) {
        ersatzServer.expectations {
            OPTIONS('/options').responds().allows(HttpMethod.GET, HttpMethod.POST).code(200)
            OPTIONS('/*').responds().allows(HttpMethod.DELETE, HttpMethod.GET, HttpMethod.OPTIONS).code(200)
        }

        HttpURLConnection connection = new URL("${ersatzServer.httpUrl}/$path").openConnection() as HttpURLConnection
        connection.requestMethod = 'OPTIONS'

        assertEquals 200, connection.responseCode
        assertEquals allowed.size(), connection.headerFields['Allow'].size()
        assertTrue connection.headerFields['Allow'].containsAll(allowed)
        assertEquals '', connection.inputStream.text
    }

    private static Stream<Arguments> optionsProvider() {
        Stream.of(
            arguments('options', ['GET', 'POST']),
            arguments('*', ['OPTIONS', 'GET', 'DELETE'])
        )
    }

    @Test void 'TRACE sends back request'() {
        ersatzServer.start()

        HttpURLConnection connection = new URL("${ersatzServer.httpUrl}/info?data=foo+bar").openConnection() as HttpURLConnection
        connection.requestMethod = 'TRACE'

        assertEquals MESSAGE_HTTP.value, connection.contentType
        assertEquals(
            """TRACE /info?data=foo+bar HTTP/1.1
            Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
            Connection: keep-alive
            User-Agent: Java/${System.getProperty('java.version')}
            Host: localhost:${ersatzServer.httpPort}
        """.readLines()*.trim(),
            connection.inputStream.text.readLines()*.trim()
        )

        assertEquals 200, connection.responseCode
    }

    @ParameterizedTest @DisplayName('delayed response (#delay)') @MethodSource('delayProvider')
    void delayedResponse(delay, long time) {
        ersatzServer.expectations {
            GET('/slow').responds().delay(delay).body('Done').code(200)
        }

        long started = currentTimeMillis()
        String response = "${ersatzServer.httpUrl}/slow".toURL().text
        long elapsed = currentTimeMillis() - started

        assertEquals 'Done', response
        assertTrue elapsed >= (time - 10) // there is some wiggle room
    }

    private static Stream<Arguments> delayProvider() {
        Stream.of(
            arguments(1000, 1000),
            arguments('PT1S', 1000)
        )
    }

    @ParameterizedTest @DisplayName('using closure as matcher (#path)')
    @CsvSource([
        '/general/one,ok',
        '/general/two,ok',
        '/other,err'
    ])
    void usingClosureAsMatcher(String path, String resp) {
        ersatzServer.expectations {
            GET({ p -> p.startsWith('/general') } as Matcher<String>).responds().body('ok').code(200)
            GET('/other').responds().body('err').code(200)
        }

        assertEquals resp, ersatzServer.httpUrl(path).toURL().text
    }

    @Test 'proxied request should return proxy not original'() {
        ErsatzServer proxyServer = new ErsatzServer({
            expectations {
                GET('/proxied').called(1).responds().body('forwarded').code(200)
            }
        })

        ersatzServer.expectations {
            GET('/proxied').called(0).responds().body('original').code(200)
        }

        OkHttpClient proxiedClient = new OkHttpClient.Builder()
            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', proxyServer.httpPort)))
            .cookieJar(new InMemoryCookieJar())
            .build()

        Response response = proxiedClient.newCall(new Request.Builder().get().url(ersatzServer.httpUrl('/proxied')).build()).execute()

        assertEquals 200, response.code()
        assertEquals 'forwarded'.bytes, response.body().bytes()

        assertTrue proxyServer.verify()
        assertTrue ersatzServer.verify()
    }

    @Test void 'multiple header matching support'() {
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

        Response response = http.get(ersatzServer.httpUrl('/api/hello'), 'Accept': ['application/json', 'application/vnd.company+json'])

        assertEquals 200, response.code()
        assertEquals '{msg=World}', response.body().string()

        assertTrue ersatzServer.verify()
    }

    @ParameterizedTest @DisplayName('multiple header matching support (using matcher)')
    @CsvSource([
        'application/vnd.company+json',
        'application/json'
    ])
    void multipleHeaderMatchingSupport(String headerValue) {
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

        Response response = http.get(ersatzServer.httpUrl('/api/hello'), 'Accept': headerValue)

        assertEquals 200, response.code()
        assertEquals '{msg=World}', response.body().string()

        assertTrue ersatzServer.verify()
    }

    @Test @DisplayName('multiple header matching support (expecting two headers and had one)')
    void multipleHeaderExpecting2Had1() {
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

        Response response = http.get(ersatzServer.httpUrl('/api/hello'), 'Accept': 'application/json')

        assertEquals 404, response.code()

        assertTrue ersatzServer.verify()
    }

    @Test @DisplayName('baking cookies')
    void bakingCookies() {
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

        Response response = http.get(ersatzServer.httpUrl('/setkermit'))
        assertEquals 'ok', response.body().string()

        response = http.get(ersatzServer.httpUrl('/showkermit'), 'Cookie': 'kermit=frog; path=/showkermit')
        assertEquals 'ok', response.body().string()

        assertTrue ersatzServer.verify()
    }

    @Test @DisplayName('variable-case headers')
    void variableCaseHeaders() {
        ersatzServer.expectations {
            POST('*') {
                body({ true } as Matcher<Object>, APPLICATION_URLENCODED)
                header('Something-Headery', 'a-value')
                responds().body('OK')
            }
        }

        Response response = http.post(
            ersatzServer.httpUrl('/postit'),
            create(parse(APPLICATION_URLENCODED.value), 'Posted'),
            'something-headery': 'a-value'
        )

        assertEquals 'OK', response.body().string()
    }

    @Test @DisplayName('post params')
    void postParams() {
        ersatzServer.expectations {
            POST('/updates') {
                param('foo', 'bar')
                responds().code(201)
            }
        }

        Response response = http.post(
            ersatzServer.httpUrl('/updates'),
            create(parse(APPLICATION_URLENCODED.value), 'foo=bar')
        )

        assertEquals 201, response.code()
    }

    @Test @DisplayName('Proper closure delegation')
    void properClosureDelegate() {
        ersatzServer.expectations {
            GET("/headers") {
                header('Accept', 'application/json')

                responder {
                    header("Bad", "code")
                    body('{"hello":"world"}', 'application/json')
                }
            }
        }

        Response response = http.get(ersatzServer.httpUrl('/headers'), Accept: 'application/json')

        assertEquals '{"hello":"world"}', response.body().string()
        assertEquals 'code', response.header('Bad')
    }

    @Test @DisplayName('Proper delegation of content body request/response')
    void properDelegationofContentBody() {
        ersatzServer.expectations {
            POST('/booga') {
                decoder TEXT_PLAIN, Decoders.utf8String
                body 'a request', TEXT_PLAIN
                responder {
                    body 'a response', TEXT_PLAIN
                }
            }
        }

        def response = http.post(
            ersatzServer.httpUrl('/booga'),
            create(parse('text/plain'), 'a request')
        )

        assertEquals 'a response', response.body().string()
    }

    @Test @DisplayName('Multiple responses for GET request')
    void multipleResponsesForGet() {
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

        def response = http.get(ersatzServer.httpUrl('/aclue'), Info: 'value')

        assertEquals 200, response.code()
        assertEquals 'Alpha', response.body().string()

        response = http.get(ersatzServer.httpUrl('/aclue'), Info: 'value')

        assertEquals 200, response.code()
        assertEquals 'Bravo', response.body().string()
    }

    @Test @DisplayName('Multiple responses for PUT request')
    void multipleResponsesForPut() {
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

        def response = http.put(ersatzServer.httpUrl('/aclue'), payload, Info: 'value')

        assertEquals 200, response.code()
        assertEquals '', response.body().string()

        response = http.put(ersatzServer.httpUrl('/aclue'), payload, Info: 'value')

        assertEquals 200, response.code()
        assertEquals 'Bravo', response.body().string()
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

