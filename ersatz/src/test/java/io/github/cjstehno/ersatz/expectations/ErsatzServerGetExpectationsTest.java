/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.expectations;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.InMemoryCookieJar;
import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.encdec.Encoders;
import io.github.cjstehno.ersatz.encdec.ErsatzMultipartResponseContent;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.match.ErsatzMatchers;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.testthings.Resources;
import lombok.RequiredArgsConstructor;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.github.cjstehno.ersatz.TestAssertions.assertNotFound;
import static io.github.cjstehno.ersatz.TestAssertions.assertOkWithString;
import static io.github.cjstehno.ersatz.TestAssertions.assertStatusWithString;
import static io.github.cjstehno.ersatz.TestAssertions.verify;
import static io.github.cjstehno.ersatz.cfg.ContentType.IMAGE_JPG;
import static io.github.cjstehno.ersatz.cfg.ContentType.MULTIPART_MIXED;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static io.github.cjstehno.ersatz.encdec.Cookie.cookie;
import static io.github.cjstehno.ersatz.encdec.MultipartResponseContent.multipartResponse;
import static io.github.cjstehno.ersatz.match.CookieMatcher.cookieMatcher;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static io.github.cjstehno.ersatz.match.PredicateMatcher.predicatedBy;
import static io.github.cjstehno.ersatz.util.BasicAuth.basicAuth;
import static io.github.cjstehno.ersatz.util.HttpClientExtension.Client.basicAuthHeader;
import static io.github.cjstehno.ersatz.util.HttpHeaders.CONTENT_DISPOSITION;
import static io.github.cjstehno.ersatz.util.HttpHeaders.CONTENT_ENCODING;
import static io.github.cjstehno.ersatz.util.HttpHeaders.COOKIE;
import static io.github.cjstehno.testthings.Resources.resourceStream;
import static io.github.cjstehno.testthings.Resources.resourceToBytes;
import static io.github.cjstehno.testthings.Resources.resourceToString;
import static java.lang.System.currentTimeMillis;
import static java.net.Proxy.Type.HTTP;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig("serverConfig")
public class ErsatzServerGetExpectationsTest {

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.https();
        cfg.encoder(IMAGE_JPG, String.class, Encoders.content);
    }

    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPath(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/something").secure(https).called(1).responds().body(responseText, TEXT_PLAIN);
        });

        assertOkWithString(responseText, client.get("/something", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathAndConsumer(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/something", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> res.body(responseText, TEXT_PLAIN));
            });
        });

        assertOkWithString(responseText, client.get("/something", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcher(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET(startsWith("/loader/")).secure(https).called(1).responds().body(responseText, TEXT_PLAIN);
        });

        assertOkWithString(responseText, client.get("/loader/insecure", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcherAndConsumer(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET(startsWith("/loader/"), req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> res.body(responseText, TEXT_PLAIN));
            });
        });

        assertOkWithString(responseText, client.get("/loader/something", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer (with response headers): https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathAndConsumerWithResponseHeaders(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/something", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.header("Alpha", "Header-A");
                    res.header("Bravo", List.of("Header-B1", "Header-B2"));
                    res.body(responseText, TEXT_PLAIN);
                });
            });
        });

        val response = client.get("/something", https);
        assertOkWithString(responseText, response);

        assertEquals("Header-A", response.header("Alpha"));
        assertEquals(List.of("Header-B1", "Header-B2"), response.headers("Bravo"));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] BASIC authentication: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withBASICAuthentication(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(cfg -> {
            cfg.GET("/safe", req -> {
                basicAuth(req, "basicuser", "ba$icp@$$");
                req.secure(https);
                req.called(1);
                req.responder(res -> res.body(responseText, TEXT_PLAIN));
            });
        });

        assertOkWithString(responseText, client.get("/safe", builder -> basicAuthHeader(builder, "basicuser", "ba$icp@$$"), https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] chunked image: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withChunkedResponse(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/chunky", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.chunked(chunk -> {
                        chunk.chunks(4);
                        chunk.delay(200);
                    });
                    res.body("/test-image.jpg", IMAGE_JPG);
                });
            });
        });

        val response = client.get("/chunky", https);

        assertEquals(200, response.code());
        assertEquals("chunked", response.header("Transfer-encoding"));
        assertArrayEquals(resourceToBytes("/test-image.jpg"), response.body().bytes());

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multipart text: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void multipartText(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/text", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.encoder(MULTIPART_MIXED, ErsatzMultipartResponseContent.class, Encoders.multipart);
                    res.body(multipartResponse(mrc -> {
                        mrc.boundary("t8xOJjySKePdRgBHYD");
                        mrc.encoder(TEXT_PLAIN.getValue(), CharSequence.class, o -> o.toString().getBytes());
                        mrc.field("alpha", "bravo");
                        mrc.part("file", "data.txt", TEXT_PLAIN, "This is some file data");
                    }));
                });
            });
        });

        val expectedLines = resourceToString("/multipart-text.txt").lines().collect(toList());
        val actualLines = client.get("/text", https).body().string().trim().lines().collect(toList());

        assertLinesMatch(expectedLines, actualLines);

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multipart binary: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void multipartBinary(final boolean https, @TempDir final File dir, final ErsatzServer server) throws Exception {
        server.expectations(expect -> {
            expect.GET("/data", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.encoder(MULTIPART_MIXED, ErsatzMultipartResponseContent.class, Encoders.multipart);
                    res.body(multipartResponse(mrc -> {
                        mrc.boundary("WyAJDTEVlYgGjdI13o");
                        mrc.encoder(TEXT_PLAIN, CharSequence.class, Encoders.text);
                        mrc.encoder("image/jpeg", InputStream.class, Encoders.content);
                        mrc.part("file", "data.txt", TEXT_PLAIN, "This is some file data");
                        mrc.part("image", "test-image.jpg", "image/jpeg", resourceStream("/test-image.jpg"), "base64");
                    }));
                });
            });
        });

        val response = client.get("/data", https);

        val down = new ResponseDownloadContent(response.body());
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

        val imageBytes = Resources.resourceToBytes("/test-image.jpg");
        assertEquals(imageBytes.length, items.get(1).getSize());
        assertArrayEquals(imageBytes, items.get(1).get());

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multipart binary (simpler): https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void multipartBinarySimpler(final boolean https, @TempDir final File dir, final ErsatzServer server) throws Exception {
        server.expectations(expect -> {
            expect.GET("/stuff", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.encoder(MULTIPART_MIXED, ErsatzMultipartResponseContent.class, Encoders.multipart);
                    res.body(multipartResponse(mrc -> {
                        mrc.boundary("WyAJDTEVlYgGjdI13o");
                        mrc.encoder(IMAGE_JPG, InputStream.class, Encoders.content);
                        mrc.part("image", "test-image.jpg", IMAGE_JPG, resourceStream("/test-image.jpg"), "base64");
                    }));
                });
            });
        });

        val response = client.get("/stuff", https);

        val down = new ResponseDownloadContent(response.body());
        FileUpload fu = new FileUpload(new DiskFileItemFactory(100000, dir));
        List<FileItem> items = fu.parseRequest(down);

        assertEquals(1, items.size());

        assertEquals("image", items.get(0).getFieldName());
        assertEquals("test-image.jpg", items.get(0).getName());
        assertEquals("image/jpeg", items.get(0).getContentType());

        val bytes = resourceToBytes("/test-image.jpg");
        assertEquals(bytes.length, items.get(0).getSize());
        assertArrayEquals(bytes, items.get(0).get());

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multiple header matching: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void multipleHeaderMatching(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/api/hello", req -> {
                req.secure(https);
                req.called(1);
                req.header("Accept", "application/json");
                req.header("Accept", "application/vnd.company+json");
                req.responder(res -> {
                    res.code(200);
                    res.body(Map.of("msg", "World"), "application/vnd.company+json");
                });
            });
        });

        val response = client.get(
            "/api/hello",
            builder -> {
                builder.addHeader("Accept", "application/json");
                builder.addHeader("Accept", "application/vnd.company+json");
            },
            https
        );

        assertEquals(200, response.code());
        assertEquals("{msg=World}", response.body().string());

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multiple header matching (matcher): https({0})")
    @CsvSource({
        "false,application/vnd.company+json",
        "false,application/json",
        "true,application/vnd.company+json",
        "true,application/json"
    })
    void multipleHeaderMatchingWithMatcher(final boolean https, final String headerValue, final ErsatzServer server) throws IOException {
        final var headerMatcher = ErsatzMatchers.functionMatcher((Function<Iterable<? super String>, Boolean>) objects -> {
            for (final var it : (Iterable<? super String>) objects) {
                if (it.equals("application/vnd.company+json") || it.equals("application/json")) {
                    return true;
                }
            }
            return false;
        });

        server.expectations(expect -> {
            expect.GET("/api/hello", req -> {
                req.secure(https);
                req.called(1);
                req.header("Accept", headerMatcher);
                req.responder(res -> {
                    res.code(200);
                    res.body(Map.of("msg", "World"), "application/vnd.company+json");
                });
            });
        });

        val response = client.get(
            "/api/hello",
            builder -> builder.header("Accept", headerValue),
            https
        );

        assertEquals(200, response.code());
        assertEquals("{msg=World}", response.body().string());

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multiple header matching (expecting two headers and had one): https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void multipleHeaderExpecting2Had1(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/api/hello", req -> {
                req.secure(https);
                req.called(0);
                req.header("Accept", "application/json");
                req.header("Accept", "application/vnd.company+json");
                req.responder(res -> {
                    res.code(200);
                    res.body(Map.of("msg", "World"), "application/vnd.company+json");
                });
            });
        });

        val response = client.get(
            "/api/hello",
            builder -> builder.header("Accept", "application/json"),
            https
        );

        assertEquals(404, response.code());

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Delayed response: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void delayedResponse(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(e -> {
            e.GET("/slow").secure(https).called(1).responds().delay("PT1S").body("Done").code(200);
        });

        val started = currentTimeMillis();
        val response = client.get("/slow", https);
        val elapsed = currentTimeMillis() - started;

        assertOkWithString("Done", response);
        assertTrue(elapsed >= 900); // there is some wiggle room

        verify(server);
    }

    @Test @DisplayName("proxied request should return proxy not original")
    void proxiedShouldReturnProxy(final ErsatzServer server) throws IOException {
        val proxyServer = new ErsatzServer(c -> c.expectations(e -> e.GET("/proxied").called(1).responds().body("forwarded").code(200)));
        try {
            server.expectations(e -> {
                e.GET("/proxied").called(0).responds().body("original").code(200);
            });

            val proxiedClient = new OkHttpClient.Builder()
                .proxy(new Proxy(HTTP, new InetSocketAddress("localhost", proxyServer.getHttpPort())))
                .cookieJar(new InMemoryCookieJar())
                .build();

            val response = proxiedClient.newCall(new Request.Builder().get().url(server.httpUrl("/proxied")).build()).execute();

            assertEquals(200, response.code());
            assertArrayEquals("forwarded".getBytes(), response.body().bytes());

            assertTrue(proxyServer.verify());
            verify(server);

        } finally {
            proxyServer.close();
        }
    }

    @ParameterizedTest(name = "[{index}] Downloading file: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void downloadingFile(final boolean https, final ErsatzServer server) throws IOException {
        final var zipBites = resourceToBytes("/images.zip");

        server.expectations(expect -> {
            expect.GET("/download", req -> {
                req.secure(https);
                req.called(1);
                req.header(CONTENT_DISPOSITION, "attachment; filename=\"data.zip\"");
                req.responds().body(zipBites, "application/zip");
            });
        });

        val response = client.get(
            "/download",
            builder -> {
                builder.header("Content-Disposition", "attachment; filename=\"data.zip\"");
            },
            https
        );

        assertEquals(200, response.code());

        val responseBytes = response.body().bytes();
        assertEquals(zipBites.length, responseBytes.length);
        assertArrayEquals(zipBites, responseBytes);

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Valueless query string: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void valuelessQueryString(final boolean https, final String responseContent, final ErsatzServer server) throws IOException {
        server.expectations(e -> {
            e.GET("/something").secure(https).called(1).query("enabled").responds().code(200).body(responseContent, TEXT_PLAIN);
        });

        assertOkWithString(responseContent, client.get("/something?enabled", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Valued query string: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void valuedQueryString(final boolean https, final String responseContent, final ErsatzServer server) throws IOException {
        server.expectations(e -> {
            e.GET("/something").secure(https).called(1).query("enabled", "yes").responds().code(200).body(responseContent, TEXT_PLAIN);
        });

        assertOkWithString(responseContent, client.get("/something?enabled=yes", https));
        assertNotFound(client.get("/something?enabled=no", https));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multiple responses: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void multipleResponsesForGet(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(e -> {
            e.GET("/aclue", req -> {
                req.secure(https);
                req.called(2);
                req.header("Info", "value");
                req.responder(res -> {
                    res.code(200);
                    res.body("Alpha", TEXT_PLAIN);
                });
                req.responder(res -> {
                    res.code(200);
                    res.body("Bravo", TEXT_PLAIN);
                });
            });
        });

        assertOkWithString("Alpha", client.get(
            "/aclue",
            builder -> builder.header("Info", "value"),
            https
        ));

        assertOkWithString("Bravo", client.get(
            "/aclue",
            builder -> builder.header("Info", "value"),
            https
        ));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Baking cookies: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void bakingCookies(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/setkermit").secure(https).called(1).responder(res -> {
                res.body("ok", TEXT_PLAIN);
                res.cookie("kermit", cookie(c -> {
                    c.value("frog");
                    c.path("/showkermit");
                }));
            });

            expect.GET("/showkermit", req -> {
                req.secure(https).called(1);
                req.cookie("kermit", cookieMatcher(cm -> {
                    cm.value(startsWith("frog"));
                }));
                req.responder(res -> {
                    res.body("ok", TEXT_PLAIN);
                    res.cookie("miss", cookie(c -> {
                        c.value("piggy");
                        c.path("/");
                    }));
                    res.cookie("fozzy", cookie(c -> {
                        c.value("bear");
                        c.path("/some/deep/path");
                    }));
                });
            });
        });

        assertOkWithString("ok", client.get("/setkermit", https));

        assertOkWithString("ok", client.get(
            "/showkermit",
            builder -> {
                builder.header(COOKIE, "kermit=frog; path=/showkermit");
            },
            https
        ));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Request matches but no response: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withListener(final boolean https, final String responseContent, final ErsatzServer server) throws IOException {
        val counter = new AtomicInteger(0);

        server.expectations(expect -> {
            expect.GET("/ears", req -> {
                req.secure(https);
                req.called(1);
                req.listener(cr -> {
                    assertEquals("/ears", cr.getPath());
                    assertEquals(HttpMethod.GET, cr.getMethod());
                    counter.incrementAndGet();
                });
                req.responds().body(responseContent, TEXT_PLAIN);
            });
        });

        val response = client.get("/ears", https);
        assertOkWithString(responseContent, response);

        assertEquals(1, await().untilAtomic(counter, equalTo(1)));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Request matches but no response: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void noResponseConfigured(final boolean https, final ErsatzServer server) throws IOException {
        server.expects().GET("/missing").secure(https).called(1);

        assertStatusWithString(204, "", client.get("/missing", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Request matches but null response: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void respondsWithNull(final boolean https, final ErsatzServer server) throws IOException {
        server.expects().GET("/missing").secure(https).called(1).responds().code(200).body(null);

        assertStatusWithString(200, "", client.get("/missing", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Gzip compression supported: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void gzipSupported(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/gzip").secure(https).called(1).header("Accept-Encoding", "gzip")
                .responds().body(responseText, TEXT_PLAIN);
        });

        val response = client.get("/gzip", https);

        assertEquals(200, response.code());
        assertTrue(response.networkResponse().headers(CONTENT_ENCODING).contains("gzip"));
        assertEquals(responseText, response.body().string());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Non-compression supported: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void nonCompressionSupported(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET("/gzip").secure(https).called(1).header("Accept-Encoding", "")
                .responds().body(responseText, TEXT_PLAIN);
        });

        val response = client.get(
            "/gzip",
            builder -> {
                builder.header("Accept-Encoding", "");
            },
            https
        );

        assertEquals(200, response.code());
        assertFalse(response.networkResponse().headers("Content-Encoding").contains("gzip"));
        assertEquals(responseText, response.body().string());
        verify(server);
    }

    @Test void pathMatchingWithPredicate(final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET(
                pathMatching(predicatedBy(
                    path -> path.toLowerCase(ROOT).startsWith("/foo")
                )),
                req -> {
                    req.responder(res -> res.code(200));
                }
            );
        });

        val response = client.get("/FOOTBALL");

        assertEquals(200, response.code());
        verify(server);
    }

    @Test void patchMatchingWithPredicateAndDescription(final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.GET(
                pathMatching(predicatedBy(
                    "a string starting with /foo (ignoring case)",
                    path -> path.toLowerCase(ROOT).startsWith("/foo")
                )),
                req -> {
                    req.responder(res -> res.code(200));
                }
            );
        });

        val response = client.get("/FOOTBALL");

        assertEquals(200, response.code());
        verify(server);
    }

    @RequiredArgsConstructor @SuppressWarnings("ClassCanBeRecord")
    private static class ResponseDownloadContent implements UploadContext {

        private final ResponseBody body;

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
