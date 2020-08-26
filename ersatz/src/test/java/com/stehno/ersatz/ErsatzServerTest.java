/**
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
package com.stehno.ersatz;

import com.stehno.ersatz.encdec.Cookie;
import com.stehno.ersatz.encdec.Encoders;
import com.stehno.ersatz.encdec.MultipartResponseContent;
import com.stehno.ersatz.server.ClientRequest;
import com.stehno.ersatz.util.HttpClient;
import okhttp3.Response;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.stehno.ersatz.cfg.ContentType.*;
import static com.stehno.ersatz.cfg.ContentType.APPLICATION_URLENCODED;
import static com.stehno.ersatz.encdec.Cookie.cookie;
import static com.stehno.ersatz.encdec.Decoders.utf8String;
import static com.stehno.ersatz.match.CookieMatcher.cookieMatcher;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ErsatzServerTest {

    // FIXME: these tests should be pulled out into more appropriate parts of the test code
    // rather than being thrown in here
    // FIXME: try using the junit extension here

    private ErsatzServer ersatzServer = new ErsatzServer(c -> {
        c.encoder(MULTIPART_MIXED, MultipartResponseContent.class, Encoders.multipart);
    });
    private HttpClient http;

    @BeforeEach void beforeEach() {
        ersatzServer.clearExpectations();
        http = new HttpClient();
    }

    @AfterEach void afterEach() {
        ersatzServer.close();
    }

    @Test void prototype() throws IOException {
        final AtomicInteger counter = new AtomicInteger();
        final Consumer<ClientRequest> listener = request -> counter.incrementAndGet();

        ersatzServer.expectations(expectations -> {
            expectations.GET("/foo").called(greaterThanOrEqualTo(1))
                .responder(response -> response.body("This is Ersatz!!"))
                .responds().body("This is another response");

            expectations.GET("/bar").called(greaterThanOrEqualTo(2)).listener(listener).responds().body("This is Bar!!");

            expectations.GET("/baz").query("alpha", "42").responds().body("The answer is 42");

            expectations.GET("/bing").header("bravo", "hello").responds().body("Heads up!").header("charlie", "goodbye").code(222);

            expectations.GET("/cookie/monster").cookie("flavor", "chocolate-chip").responds().body("I love cookies!").cookie("eaten", "yes");

            expectations.HEAD("/head").responds().header("foo", "blah").code(123);

            expectations.POST("/form").body("some content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, utf8String)
                .responds().body("response");

            expectations.PUT("/update").body("more content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, utf8String)
                .responds().body("updated");

            expectations.DELETE("/remove").responds().body("removed");

            expectations.POST("/patch").body("a change", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, utf8String)
                .responds().body("patched");
        });

        assertEquals("This is Ersatz!!", http.get(ersatzServer.httpUrl("/foo")).body().string());

        assertEquals("This is another response", http.get(ersatzServer.httpUrl("/foo")).body().string());

        assertEquals("This is Bar!!", http.get(ersatzServer.httpUrl("/bar")).body().string());
        assertEquals("This is Bar!!", http.get(ersatzServer.httpUrl("/bar")).body().string());

        await().untilAtomic(counter, equalTo(2));

        assertEquals("The answer is 42", http.get(ersatzServer.httpUrl("/baz?alpha=42")).body().string());

        okhttp3.Response resp = http.get(singletonMap("bravo", "hello"), ersatzServer.httpUrl("/bing"));
        assertEquals(222, resp.code());
        assertEquals("goodbye", resp.header("charlie"));
        assertEquals("Heads up!", resp.body().string());

        resp = http.get(singletonMap("Cookie", "flavor=chocolate-chip"), ersatzServer.httpUrl("/cookie/monster"));
        assertEquals("I love cookies!", resp.body().string());
        assertEquals("eaten=yes", resp.header("Set-Cookie"));

        resp = http.head(ersatzServer.httpUrl("/head"));
        assertEquals(123, resp.code());
        assertEquals("blah", resp.header("foo"));

        resp = http.post(ersatzServer.httpUrl("/form"), create(parse("text/plain"), "some content"));
        assertEquals("response", resp.body().string());

        resp = http.put(ersatzServer.httpUrl("/update"), create(parse("text/plain"), "more content"));
        assertEquals("updated", resp.body().string());

        resp = http.delete(ersatzServer.httpUrl("/remove"));
        assertEquals("removed", resp.body().string());

        resp = http.post(ersatzServer.httpUrl("/patch"), create(parse("text/plain"), "a change"));
        assertEquals("patched", resp.body().string());

        assertTrue(ersatzServer.verify());
    }

    @Test void alternateConfiguration() throws IOException {
        ErsatzServer server = new ErsatzServer(config -> {
            config.expects().GET(startsWith("/hello")).responds().body("ok");
        }).start();

        assertEquals("ok", http.get(server.getHttpUrl() + "/hello/there").body().string());

        server.stop();
    }

    @Test @DisplayName("Request matches but there is no response")
    void no_response_configured() throws IOException {
        final var server = new ErsatzServer(config -> {
            config.expects().GET("/missing");
        }).start();

        final var response = http.get(server.httpUrl("/missing"));
        assertEquals(204, response.code());
        assertEquals("", response.body().string());

        server.stop();
    }

    @Test @DisplayName("Request matches but sends null as response")
    void responds_with_null() throws IOException {
        final var server = new ErsatzServer(config -> {
            config.expects().GET("/missing").responds().code(200).body(null);
        }).start();

        final var response = http.get(server.httpUrl("/missing"));
        assertEquals(200, response.code());
        assertEquals("", response.body().string());

        server.stop();
    }

    @Test @DisplayName("downloading file with GET")
    void downloading_with_get() throws IOException {
        final var zipBites = ErsatzServerTest.class.getResourceAsStream("/images.zip").readAllBytes();

        final var server = new ErsatzServer(cfg -> {
            cfg.expectations(exp -> {
                exp.GET("/download", req -> {
                    req.header("Content-Disposition", "attachment; filename=\"data.zip\"");
                    req.responder(res -> {
                        res.body(zipBites, "application/zip");
                    });
                });
            });
        });

        final var response = http.get(
            Map.of("Content-Disposition", "attachment; filename=\"data.zip\""),
            server.httpUrl("/download")
        );

        assertEquals(200, response.code());

        final var responseBytes = response.body().bytes();
        assertEquals(zipBites.length, responseBytes.length);
        assertArrayEquals(zipBites, responseBytes);
    }

    @Test @DisplayName("not started should give useful error")
    void not_started() {
        final var thrown = assertThrows(IllegalStateException.class, () -> ersatzServer.httpUrl("/nothing"));
        assertEquals("The port (-1) is invalid: Has the server been started?", thrown.getMessage());
    }

    @Test @DisplayName("chunked response: #chunkDelay")
    void chunkedResponseWithDelay() throws IOException {
        ersatzServer.timeout(1, MINUTES);
        ersatzServer.expectations(e -> {
            e.GET("/chunky").responder(res -> {
                res.body("This is chunked content", TEXT_PLAIN);
                res.chunked(ch -> {
                    ch.chunks(3);
                    ch.delay(15);
                });
            });
        });

        final var response = http.get(ersatzServer.httpUrl("/chunky"));

        assertEquals("chunked", response.header("Transfer-encoding"));
        assertEquals("This is chunked content", response.body().string());
    }

    @Test @DisplayName("valueless query string param") void valuelessQueryString() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/something").query("enabled").responds().code(200).body("OK", TEXT_PLAIN);
        });

        final var response = http.get(ersatzServer.httpUrl("/something?enabled"));

        assertEquals(200, response.code());
        assertEquals("OK", response.body().string());
        assertTrue(ersatzServer.verify());
    }

    @Test @DisplayName("gzip compression supported") void gzipSupported() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/gzip").header("Accept-Encoding", "gzip").responds().body('x' * 1000, TEXT_PLAIN);
        });

        Response response = http.get(ersatzServer.httpUrl("/gzip"));

        assertEquals(200, response.code());
        assertTrue(response.networkResponse().headers("Content-Encoding").contains("gzip"));
    }

    @Test @DisplayName("non-compression supported") void nonCompressionSupported() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/gzip").header("Accept-Encoding", "").responds().body('x' * 1000, TEXT_PLAIN);
        });

        Response response = http.get(
            Map.of("Accept-Encoding", ""),
            ersatzServer.httpUrl("/gzip")
        );

        assertEquals(200, response.code());
        assertFalse(response.networkResponse().headers("Content-Encoding").contains("gzip"));
    }

    @Test @DisplayName("deflate supported") void deflateSupported() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/gzip").header("Accept-Encoding", "deflate").responds().body('x' * 1000, TEXT_PLAIN);
        });

        Response response = http.get(
            Map.of("Accept-Encoding", "deflate"),
            ersatzServer.httpUrl("/gzip")
        );

        assertEquals(200, response.code());
        assertTrue(response.networkResponse().headers("Content-Encoding").contains("deflate"));
    }

    @Test @DisplayName("Multiple responses for GET request")
    void multipleResponsesForGet() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/aclue", req -> {
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

        var response = http.get(Map.of("Info", "value"), ersatzServer.httpUrl("/aclue"));

        assertEquals(200, response.code());
        assertEquals("Alpha", response.body().string());

        response = http.get(Map.of("Info", "value"), ersatzServer.httpUrl("/aclue"));

        assertEquals(200, response.code());
        assertEquals("Bravo", response.body().string());
    }

    @Test @DisplayName("Multiple responses for PUT request")
    void multipleResponsesForPut() throws IOException {
        ersatzServer.expectations(e -> {
            e.PUT("/aclue", req -> {
                req.header("Info", "value");
                req.responder(res -> {
                    res.code(200);
                });
                req.responder(res -> {
                    res.code(200);
                    res.body("Bravo", TEXT_PLAIN);
                });
            });
        });

        var payload = create(parse("text/plain"), "payload");
        var response = http.put(Map.of("Info", "value"), ersatzServer.httpUrl("/aclue"), payload);

        assertEquals(200, response.code());
        assertEquals("", response.body().string());

        response = http.put(Map.of("Info", "value"), ersatzServer.httpUrl("/aclue"), payload);

        assertEquals(200, response.code());
        assertEquals("Bravo", response.body().string());
    }

    @Test @DisplayName("variable-case headers")
    void variableCaseHeaders() throws IOException {
        ersatzServer.expectations(e -> e.POST("*", req -> {
            req.body(CoreMatchers.anything(), APPLICATION_URLENCODED);
            req.header("Something-Headery", "a-value");
            req.responds().body("OK");
        }));

        var response = http.post(
            Map.of("something-headery", "a-value"),
            ersatzServer.httpUrl("/postit"),
            create(parse(APPLICATION_URLENCODED.getValue()), "Posted")
        );

        assertEquals("OK", response.body().string());
    }

    @Test @DisplayName("post params")
    void postParams() throws IOException {
        ersatzServer.expectations(e -> {
            e.POST("/updates", req -> {
                req.param("foo", "bar");
                req.responds().code(201);
            });
        });

        var response = http.post(
            ersatzServer.httpUrl("/updates"),
            create(parse(APPLICATION_URLENCODED.getValue()), "foo=bar")
        );

        assertEquals(201, response.code());
    }

    @Test @DisplayName("baking cookies")
    void bakingCookies() throws IOException {
        ersatzServer.expectations(e -> {
            e.GET("/setkermit").called(1).responder(res -> {
                res.body("ok", TEXT_PLAIN);
                res.cookie("kermit", cookie(c -> {
                    c.value("frog");
                    c.path("/showkermit");
                }));
            });

            e.GET("/showkermit", req -> {
                req.called(1);
                req.cookie("kermit", cookieMatcher(cm -> {
                    cm.value(startsWith("frog"));
                }));
                req.responder(res -> {
                    res.body("ok", TEXT_PLAIN);
                    res.cookie("miss", Cookie.cookie(c -> {
                        c.value("piggy");
                        c.path("/");
                    }));
                    res.cookie("fozzy", Cookie.cookie(c -> {
                        c.value("bear");
                        c.path("/some/deep/path");
                    }));
                });
            });
        });

        var response = http.get(ersatzServer.httpUrl("/setkermit"));
        assertEquals("ok", response.body().string());

        response = http.get(Map.of("Cookie", "kermit=frog; path=/showkermit"), ersatzServer.httpUrl("/showkermit"));
        assertEquals("ok", response.body().string());

        assertTrue(ersatzServer.verify());
    }
}
