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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static com.stehno.ersatz.TestAssertions.verify;
import static com.stehno.ersatz.cfg.ContentType.IMAGE_GIF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class ErsatzServerPatchExpectationsTest {

    private static final byte[] BYTES_PAYLOAD = "fear is the mind killer, the little death...".getBytes(UTF_8);
    private final ErsatzServer server = new ErsatzServer(cfg -> {
        cfg.https();
        cfg.decoder(IMAGE_GIF, Decoders.passthrough);
    });
    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttps")
    void withPath(final boolean https) throws IOException {
        server.expects().PATCH("/something").body(BYTES_PAYLOAD, IMAGE_GIF).secure(https).called(1).responds().code(200);

        assertEquals(
            200,
            client.patch(
                "/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttps")
    void withPathAndConsumer(final boolean https) throws IOException {
        server.expectations(expect -> {
            expect.PATCH("/something", req -> {
                req.secure(https);
                req.called(1);
                req.body(BYTES_PAYLOAD, IMAGE_GIF);
                req.responds().code(200);
            });
        });

        assertEquals(
            200,
            client.patch(
                "/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttps")
    void withPathMatcher(final boolean https) throws IOException {
        server.expectations(expect -> {
            expect.PATCH(startsWith("/loader/")).body(BYTES_PAYLOAD, IMAGE_GIF).secure(https).called(1)
                .responds().code(200);
        });

        assertEquals(
            200,
            client.patch(
                "/loader/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttps")
    void withPathMatcherAndConsumer(final boolean https) throws IOException {
        server.expectations(expect -> {
            expect.PATCH(startsWith("/loader/"), req -> {
                req.body(BYTES_PAYLOAD, IMAGE_GIF);
                req.secure(https);
                req.called(1);
                req.responds().code(200);
            });
        });

        assertEquals(
            200,
            client.patch(
                "/loader/something",
                create(BYTES_PAYLOAD, parse(IMAGE_GIF.getValue())),
                https
            ).code()
        );
        verify(server);
    }
}
