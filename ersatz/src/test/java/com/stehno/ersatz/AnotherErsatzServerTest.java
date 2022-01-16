/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package com.stehno.ersatz;

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.encdec.Encoders;
import com.stehno.ersatz.encdec.ErsatzMultipartResponseContent;
import com.stehno.ersatz.encdec.MultipartResponseContent;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.match.ErsatzMatchers;
import com.stehno.ersatz.util.HttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.stehno.ersatz.cfg.ContentType.*;
import static com.stehno.ersatz.cfg.HttpMethod.DELETE;
import static com.stehno.ersatz.cfg.HttpMethod.GET;
import static com.stehno.ersatz.encdec.MultipartResponseContent.multipartResponse;
import static com.stehno.ersatz.match.ErsatzMatchers.functionMatcher;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(ErsatzServerExtension.class)
class AnotherErsatzServerTest {

    // FIXME: refactor this and the other one into more contextual test suites

    private ErsatzServer ersatzServer = new ErsatzServer(c -> {
        c.encoder(MULTIPART_MIXED, MultipartResponseContent.class, Encoders.multipart);
    });
    private HttpClient http;

    @BeforeEach void beforeEach() {
        http = new HttpClient();
    }

    @Test @DisplayName("multipart text") void multipartText() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/data", req -> {
                req.responder(res -> {
                    res.encoder(MULTIPART_MIXED, ErsatzMultipartResponseContent.class, Encoders.multipart);
                    res.body(multipartResponse(mrc -> {
                        mrc.boundary("t8xOJjySKePdRgBHYD");
                        mrc.encoder(TEXT_PLAIN.getValue(), CharSequence.class, (o) -> o.toString().getBytes());
                        mrc.field("alpha", "bravo");
                        mrc.part("file", "data.txt", TEXT_PLAIN, "This is some file data");
                    }));
                });
            });
        });

        Response response = http.get(ersatzServer.httpUrl("/data"));

        var expectedLines = IOUtils.readLines(AnotherErsatzServerTest.class.getResourceAsStream("/multipart-text.txt"));
        var actualLines = response.body().string().trim().lines().collect(toList());

        assertLinesMatch(expectedLines, actualLines);
    }

    @Test @DisplayName("multipart binary")
    void multipartBinary(@TempDir final File dir) throws IOException, FileUploadException {
        ersatzServer.expectations(e -> {
            e.GET("/data", req -> {
                req.responder(res -> {
                    res.encoder(MULTIPART_MIXED, ErsatzMultipartResponseContent.class, Encoders.multipart);
                    res.body(multipartResponse(mrc -> {
                        mrc.boundary("WyAJDTEVlYgGjdI13o");
                        mrc.encoder(TEXT_PLAIN, CharSequence.class, Encoders.text);
                        mrc.encoder("image/jpeg", InputStream.class, Encoders.inputStream);
                        mrc.part("file", "data.txt", TEXT_PLAIN, "This is some file data");
                        mrc.part("image", "test-image.jpg", "image/jpeg", AnotherErsatzServerTest.class.getResourceAsStream("/test-image.jpg"), "base64");
                    }));
                });
            });
        });

        Response response = http.get(ersatzServer.httpUrl("/data"));

        var down = new ResponseDownloadContent(response.body());
        FileUpload fu = new FileUpload(new DiskFileItemFactory(100000, dir));
        List<FileItem> items = fu.parseRequest(down);

        assertEquals(2, items.size());

        assertEquals("file", items.get(0).getFieldName());
        assertEquals("data.txt", items.get(0).getName());
        assertEquals("text/plain", items.get(0).getContentType());
        assertEquals(22, items.get(0).get().length);

        assertEquals("image", items.get(1).getFieldName());
        assertEquals("test-image.jpg", items.get(1).getName());
        assertEquals("image/jpeg", items.get(1).getContentType());

        final var stream = AnotherErsatzServerTest.class.getResourceAsStream("/test-image.jpg");
        final var imageBytes = IOUtils.toByteArray(stream);
        assertEquals(imageBytes.length, items.get(1).getSize());
        assertArrayEquals(imageBytes, items.get(1).get());
    }

    @Test @DisplayName("multipart binary (simpler)")
    void multipartBinarySimpler(@TempDir final File dir) throws FileUploadException, IOException {
        ersatzServer.expectations(e -> {
            e.GET("/stuff", req -> {
                req.responder(res -> {
                    res.encoder(MULTIPART_MIXED, ErsatzMultipartResponseContent.class, Encoders.multipart);
                    res.body(multipartResponse(mrc -> {
                        mrc.boundary("WyAJDTEVlYgGjdI13o");
                        mrc.encoder(IMAGE_JPG, InputStream.class, Encoders.inputStream);
                        mrc.part("image", "test-image.jpg", IMAGE_JPG, AnotherErsatzServerTest.class.getResourceAsStream("/test-image.jpg"), "base64");
                    }));
                });
            });
        });

        Response response = http.get(ersatzServer.httpUrl("/stuff"));

        var down = new ResponseDownloadContent(response.body());
        FileUpload fu = new FileUpload(new DiskFileItemFactory(100000, dir));
        List<FileItem> items = fu.parseRequest(down);

        assertEquals(1, items.size());

        assertEquals("image", items.get(0).getFieldName());
        assertEquals("test-image.jpg", items.get(0).getName());
        assertEquals("image/jpeg", items.get(0).getContentType());

        final var stream = AnotherErsatzServerTest.class.getResourceAsStream("/test-image.jpg");
        final var bytes = stream.readAllBytes();
        assertEquals(bytes.length, items.get(0).getSize());
        assertArrayEquals(bytes, items.get(0).get());
    }

    @ParameterizedTest @DisplayName("OPTIONS #path allows #allowed") @MethodSource("optionsProvider")
    void optionsPathAllows(String path, Collection<String> allowed) throws IOException {
        ersatzServer.expectations(e -> {
            e.OPTIONS("/options").responds().allows(GET, HttpMethod.POST).code(200);
            e.OPTIONS("/*").responds().allows(DELETE, GET, HttpMethod.OPTIONS).code(200);
        });

        final var response = http.options(ersatzServer.httpUrl(path));

        assertEquals(200, response.code());
        assertEquals(allowed.size(), response.headers("Allow").size());
        assertTrue(response.headers("Allow").containsAll(allowed));
        assertArrayEquals(new byte[0], response.body().bytes());
    }

    private static Stream<Arguments> optionsProvider() {
        return Stream.of(
            arguments("/options", List.of("GET", "POST")),
            arguments("/*", List.of("OPTIONS", "GET", "DELETE"))
        );
    }

    @Test @DisplayName("TRACE sends back request") void traceSendsBackRequest() throws IOException {
        ersatzServer.start();

        final var response = http.trace(ersatzServer.httpUrl("/info?data=foo+bar"));

        assertEquals(MESSAGE_HTTP.getValue(), response.body().contentType().toString());
        assertEquals(200, response.code());

        final var expected = IOUtils.toString(AnotherErsatzServerTest.class.getResourceAsStream("/trace.txt"))
            .replace("{port}", String.valueOf(ersatzServer.getHttpPort()));

        final var expectedLines = expected.lines().collect(toList());
        final var actualLines = response.body().string().lines().collect(toList());

        assertLinesMatch(expectedLines, actualLines);
    }

    @Test @DisplayName("delayed response (#delay)")
    void delayedResponse() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/slow").responds().delay("PT1S").body("Done").code(200);
        });

        long started = currentTimeMillis();
        final var response = http.get(ersatzServer.httpUrl("/slow"));
        long elapsed = currentTimeMillis() - started;

        assertEquals("Done", response.body().string());
        assertTrue(elapsed >= 900); // there is some wiggle room
    }


    @Test @DisplayName("proxied request should return proxy not original")
    void proxiedShouldReturnProxy() throws IOException {
        final var proxyServer = new ErsatzServer(c -> {
            c.expectations(e -> {
                e.GET("/proxied").called(1).responds().body("forwarded").code(200);
            });
        });

        ersatzServer.expectations(e -> {
            e.GET("/proxied").called(0).responds().body("original").code(200);
        });

        final var proxiedClient = new OkHttpClient.Builder()
            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyServer.getHttpPort())))
            .cookieJar(new InMemoryCookieJar())
            .build();

        Response response = proxiedClient.newCall(new Request.Builder().get().url(ersatzServer.httpUrl("/proxied")).build()).execute();

        assertEquals(200, response.code());
        assertArrayEquals("forwarded".getBytes(), response.body().bytes());

        assertTrue(proxyServer.verify());
        assertTrue(ersatzServer.verify());

        proxyServer.close();
    }

    @Test @DisplayName("multiple header matching support") void multipleHeaderMatching() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/api/hello", req -> {
                req.called(1);
                req.header("Accept", "application/json");
                req.header("Accept", "application/vnd.company+json");
                req.responder(res -> {
                    res.code(200);
                    res.body(Map.of("msg", "World"), "application/vnd.company+json");
                });
            });
        });

        Response response = http.get(
            Map.of("Accept", List.of("application/json", "application/vnd.company+json")),
            ersatzServer.httpUrl("/api/hello")
        );

        assertEquals(200, response.code());
        assertEquals("{msg=World}", response.body().string());

        assertTrue(ersatzServer.verify());
    }

    @ParameterizedTest @DisplayName("multiple header matching support (using matcher)")
    @CsvSource({
        "application/vnd.company+json",
        "application/json"
    })
    void multipleHeaderMatchingSupport(String headerValue) throws IOException {
        final var headerMatcher = functionMatcher((Function<Iterable<? super String>, Boolean>) objects -> {
            for (final var it : (Iterable<? super String>) objects) {
                if (it.equals("application/vnd.company+json") || it.equals("application/json")) {
                    return true;
                }
            }
            return false;
        });

        ersatzServer.expectations(e -> {
            e.GET("/api/hello", req -> {
                req.called(1);
                req.header("Accept", headerMatcher);
                req.responder(res -> {
                    res.code(200);
                    res.body(Map.of("msg", "World"), "application/vnd.company+json");
                });
            });
        });

        Response response = http.get(Map.of("Accept", headerValue), ersatzServer.httpUrl("/api/hello"));

        assertEquals(200, response.code());
        assertEquals("{msg=World}", response.body().string());

        assertTrue(ersatzServer.verify());
    }

    @Test @DisplayName("multiple header matching support (expecting two headers and had one)")
    void multipleHeaderExpecting2Had1() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/api/hello", req -> {
                req.called(0);
                req.header("Accept", "application/json");
                req.header("Accept", "application/vnd.company+json");
                req.responder(res -> {
                    res.code(200);
                    res.body(Map.of("msg", "World"), "application/vnd.company+json");
                });
            });
        });

        Response response = http.get(
            Map.of("Accept", "application/json"),
            ersatzServer.httpUrl("/api/hello")
        );

        assertEquals(404, response.code());

        assertTrue(ersatzServer.verify());
    }

    private static class ResponseDownloadContent implements UploadContext {

        private final ResponseBody body;

        public ResponseDownloadContent(ResponseBody body) {
            this.body = body;
        }

        @Override public long contentLength() {
            return body.contentLength();
        }

        @Override public String getCharacterEncoding() {
            return body.contentType().charset() != null ? body.contentType().charset().toString() : null;
        }

        @Override public String getContentType() {
            return body.contentType().toString();
        }

        @Override public int getContentLength() {
            return (int) body.contentLength();
        }

        @Override public InputStream getInputStream() throws IOException {
            return body.byteStream();
        }
    }
}

