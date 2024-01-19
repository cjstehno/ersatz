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
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.encdec.Decoders;
import io.github.cjstehno.ersatz.encdec.Encoders;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static io.github.cjstehno.ersatz.TestAssertions.assertOkWithString;
import static io.github.cjstehno.ersatz.TestAssertions.assertStatusWithString;
import static io.github.cjstehno.ersatz.TestAssertions.verify;
import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_URLENCODED;
import static io.github.cjstehno.ersatz.cfg.ContentType.IMAGE_JPG;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig("serverConfig")
public class ErsatzServerPostExpectationsTest {

    private static final String TEXT_PAYLOAD = "this is some text!";

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.logResponseContent();
        cfg.https();
        cfg.decoder(TEXT_PLAIN, Decoders.string(UTF_8));
        cfg.decoder(APPLICATION_URLENCODED, Decoders.passthrough);
        cfg.encoder(TEXT_PLAIN, String.class, Encoders.text(UTF_8));
    }

    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] layered encoders/decoders: https({0})")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void layeredEncodingDecoding(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.POST("/posting", req -> {
                req.secure(https);
                req.called(1);
                // this decoder overrides the one defined at the server level
                req.decoder(
                    TEXT_PLAIN,
                    (bytes, context) -> "Double-decoded: " + Decoders.string(UTF_8).apply(bytes, context)
//                    (bytes, context) -> "Double-decoded: " + context.getDecoderChain().resolve(TEXT_PLAIN).apply(bytes, context)
                );
                req.body("Double-decoded: " + TEXT_PAYLOAD, TEXT_PLAIN);
                req.responder(res -> {
                    // this encoder overrides the one defined at the server level
                    res.encoder(
                        TEXT_PLAIN,
                        String.class,
                        o -> ("Double-encoded: " + o.toString()).getBytes(UTF_8)
                    );
                    res.code(200);
                    res.body("Some Content", TEXT_PLAIN);
                });
            });
        });

        val response = client.post("/posting", create(TEXT_PAYLOAD, parse("text/plain; charset=utf-8")), https);
        assertOkWithString("Double-encoded: Some Content", response);
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPath(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expects().POST("/something")
            .body(TEXT_PAYLOAD, TEXT_PLAIN.withCharset("utf-8")).secure(https).called(1)
            .responds().body(responseText, TEXT_PLAIN);

        assertOkWithString(
            responseText,
            client.post("/something", create(TEXT_PAYLOAD, parse("text/plain; charset=utf-8")), https)
        );
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathAndConsumer(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.POST("/something", req -> {
                req.secure(https);
                req.called(1);
                req.body(TEXT_PAYLOAD, "text/plain");
                req.responder(res -> {
                    res.body(responseText, TEXT_PLAIN);
                });
            });
        });

        val requestBody = create(TEXT_PAYLOAD, parse("text/plain"));
        assertOkWithString(responseText, client.post("/something", requestBody, https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcher(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.POST(startsWith("/loader/")).body(TEXT_PAYLOAD, TEXT_PLAIN).secure(https).called(1).responds().body(responseText, TEXT_PLAIN);
        });

        val requestBody = create(TEXT_PAYLOAD, parse("text/plain"));
        assertOkWithString(responseText, client.post("/loader/something", requestBody, https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcherAndConsumer(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.POST(startsWith("/loader/"), req -> {
                req.body(TEXT_PAYLOAD, TEXT_PLAIN);
                req.secure(https);
                req.called(1);
                req.responder(res -> res.body(responseText, TEXT_PLAIN));
            });
        });

        val requestBody = create(TEXT_PAYLOAD, parse("text/plain"));
        assertOkWithString(responseText, client.post("/loader/something", requestBody, https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] variable case headers: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void variableCaseHeaders(final boolean https, final String responseContent, final ErsatzServer server) throws IOException {
        server.expectations(expect -> expect.POST("*", req -> {
            req.secure(https);
            req.called(3);
            req.body(anything(), APPLICATION_URLENCODED);
            req.header("Something-Headery", "a-value");
            req.responds().body(responseContent);
        }));

        assertOkWithString(responseContent, client.post(
            "/postit",
            builder -> builder.header("something-headery", "a-value"),
            create(TEXT_PAYLOAD, parse(APPLICATION_URLENCODED.getValue())),
            https
        ));

        assertOkWithString(responseContent, client.post(
            "/postit",
            builder -> builder.header("SOMETHING-headery", "a-value"),
            create(TEXT_PAYLOAD, parse(APPLICATION_URLENCODED.getValue())),
            https
        ));

        assertOkWithString(responseContent, client.post(
            "/postit",
            builder -> builder.header("Something-Headery", "a-value"),
            create(TEXT_PAYLOAD, parse(APPLICATION_URLENCODED.getValue())),
            https
        ));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] post params: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void postParams(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.POST("/updates", req -> {
                req.secure(https);
                req.called(1);

                // testing all three versions here - redundant, but ok for testing
                req.param("foo", "bar");
                req.param("foo", List.of("bar"));
                req.param("foo", hasItem("bar"));

                req.responds().code(201);
            });
        });

        val response = client.post(
            "/updates",
            create("foo=bar", parse(APPLICATION_URLENCODED.getValue())),
            https
        );

        assertEquals(201, response.code());
        verify(server);
    }

    @Test @DisplayName("handling exception in encoder")
    void encoderErrorHandling(final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.POST("/foo", req -> {
                req.body(TEXT_PAYLOAD, TEXT_PLAIN);
                req.responder(res -> {
                    // we configure the encoder improperly - it causes an exception when used
                    res.encoder(IMAGE_JPG, Date.class, Encoders.content);
                    res.body(new Date(), IMAGE_JPG);
                });
            });
        });

        // the logs will have more detailed error messages
        val response = client.post("/foo", create(TEXT_PAYLOAD, parse("text/plain")));
        assertStatusWithString(500, "", response);
    }
}
