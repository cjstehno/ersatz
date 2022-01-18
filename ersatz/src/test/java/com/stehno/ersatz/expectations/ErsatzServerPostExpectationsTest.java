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
package com.stehno.ersatz.expectations;

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.encdec.Decoders;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.util.HttpClientExtension;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static com.stehno.ersatz.TestAssertions.assertOkWithString;
import static com.stehno.ersatz.TestAssertions.verify;
import static com.stehno.ersatz.cfg.ContentType.APPLICATION_URLENCODED;
import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class ErsatzServerPostExpectationsTest {

    private static final String TEXT_PAYLOAD = "this is some text!";
    private final ErsatzServer server = new ErsatzServer(cfg -> {
        cfg.logResponseContent();
        cfg.https();
        cfg.decoder(TEXT_PLAIN, Decoders.string(UTF_8));
        cfg.decoder(APPLICATION_URLENCODED, Decoders.passthrough);
    });
    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPath(final boolean https, final String responseText) throws IOException {
        server.expects().POST("/something").body(TEXT_PAYLOAD, TEXT_PLAIN.withCharset("utf-8")).secure(https).called(1).responds().body(responseText, TEXT_PLAIN);

        val mediaType = parse("text/plain; charset=utf-8");
        val requestBody = create(TEXT_PAYLOAD, mediaType);
        assertOkWithString(responseText, client.post("/something", requestBody, https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathAndConsumer(final boolean https, final String responseText) throws IOException {
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
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcher(final boolean https, final String responseText) throws IOException {
        server.expectations(expect -> {
            expect.POST(startsWith("/loader/")).body(TEXT_PAYLOAD, TEXT_PLAIN).secure(https).called(1).responds().body(responseText, TEXT_PLAIN);
        });

        val requestBody = create(TEXT_PAYLOAD, parse("text/plain"));
        assertOkWithString(responseText, client.post("/loader/something", requestBody, https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcherAndConsumer(final boolean https, final String responseText) throws IOException {
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
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void variableCaseHeaders(final boolean https, final String responseContent) throws IOException {
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
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttps")
    void postParams(final boolean https) throws IOException {
        server.expectations(expect -> {
            expect.POST("/updates", req -> {
                req.secure(https);
                req.called(1);
                req.param("foo", "bar");
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
}
