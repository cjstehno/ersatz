/**
 * Copyright (C) 2023 Christopher J. Stehno
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

import static io.github.cjstehno.ersatz.TestAssertions.verify;
import static io.github.cjstehno.ersatz.cfg.ContentType.IMAGE_GIF;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.encdec.Decoders;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import java.io.IOException;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig("serverConfig")
public class ErsatzServerPutExpectationsTest {

    private static final byte[] BYTES_PAYLOAD = "fear is the mind killer, the little death...".getBytes(UTF_8);

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.https();
        cfg.decoder(IMAGE_GIF, Decoders.passthrough);
    }

    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPath(final boolean https, final ErsatzServer server) throws IOException {
        server.expects().PUT("/something").body(BYTES_PAYLOAD, IMAGE_GIF).secure(https).called(1).responds().code(200);

        assertEquals(
            200,
            client.put(
                "/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathAndConsumer(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.PUT("/something", req -> {
                req.secure(https);
                req.called(1);
                req.body(BYTES_PAYLOAD, IMAGE_GIF);
                req.responds().code(200);
            });
        });

        assertEquals(
            200,
            client.put(
                "/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathMatcher(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.PUT(startsWith("/loader/")).body(BYTES_PAYLOAD, IMAGE_GIF).secure(https).called(1)
                .responds().code(200);
        });

        assertEquals(
            200,
            client.put(
                "/loader/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathMatcherAndConsumer(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.PUT(startsWith("/loader/"), req -> {
                req.body(BYTES_PAYLOAD, IMAGE_GIF);
                req.secure(https);
                req.called(1);
                req.responds().code(200);
            });
        });

        assertEquals(
            200,
            client.put(
                "/loader/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] Multiple responses: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void multipleResponsesForPut(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.PUT("/aclue", req -> {
                req.secure(https);
                req.called(3);
                req.header("Info", "value");
                req.responder(res -> res.code(200));
                req.responder(res -> res.code(201));
            });
        });

        val payload = create("payload", parse(TEXT_PLAIN.getValue()));
        assertEquals(200, client.put(
            "/aclue",
            builder -> builder.header("Info", "value"),
            payload,
            https
        ).code());

        assertEquals(201, client.put(
            "/aclue",
            builder -> builder.header("Info", "value"),
            payload,
            https
        ).code());

        assertEquals(201, client.put(
            "/aclue",
            builder -> builder.header("Info", "value"),
            payload,
            https
        ).code());

        verify(server);
    }
}
