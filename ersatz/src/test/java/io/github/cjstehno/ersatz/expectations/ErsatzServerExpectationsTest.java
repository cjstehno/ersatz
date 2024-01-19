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
import io.github.cjstehno.ersatz.cfg.RequestWithContent;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.encdec.Decoders;
import io.github.cjstehno.ersatz.encdec.Encoders;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static io.github.cjstehno.ersatz.TestAssertions.assertOkWithString;
import static io.github.cjstehno.ersatz.TestAssertions.verify;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.HEAD;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig("serverConfig")
public class ErsatzServerExpectationsTest {

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.https();
        cfg.decoder(TEXT_PLAIN, Decoders.string(UTF_8));
        cfg.encoder(TEXT_PLAIN, String.class, Encoders.text(UTF_8));
    }

    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathGet(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.request(GET, "/something").secure(https).called(1).responds().body(responseText, TEXT_PLAIN);
        });

        assertOkWithString(responseText, client.get("/something", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathPost(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.request(POST, "/something", req -> {
                req.called(1);
                req.secure(https);
                ((RequestWithContent) req).body("hello", TEXT_PLAIN);
                req.responds().body(responseText, TEXT_PLAIN);
            });
        });

        assertOkWithString(responseText, client.post("/something", create("hello", parse(TEXT_PLAIN.getValue())), https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathAndConsumer(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.request(HEAD, "/something", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.header("X-Foo", "bar");
                    res.code(200);
                });
            });
        });

        try (val response = client.head("/something", https)) {
            assertEquals(200, response.code());
            assertEquals("bar", response.header("X-Foo"));
        }

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcher(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.request(GET, startsWith("/loader/")).secure(https).called(1).responds().body(responseText, TEXT_PLAIN);
        });

        assertOkWithString(responseText, client.get("/loader/insecure", https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcherAndConsumer(final boolean https, final String responseText, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.request(GET, startsWith("/loader/"), req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> res.body(responseText, TEXT_PLAIN));
            });
        });

        assertOkWithString(responseText, client.get("/loader/insecure", https));
        verify(server);
    }
}
