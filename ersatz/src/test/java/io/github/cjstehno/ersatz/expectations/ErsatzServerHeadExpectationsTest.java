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

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;

import static io.github.cjstehno.ersatz.TestAssertions.verify;
import static io.github.cjstehno.ersatz.util.BasicAuth.basicAuth;
import static io.github.cjstehno.ersatz.util.HttpClientExtension.Client.basicAuthHeader;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig("serverConfig")
public class ErsatzServerHeadExpectationsTest {

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.https();
    }

    @SuppressWarnings("unused") private Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPath(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.HEAD("/something").secure(https).called(1).responds().code(200);
        });

        assertEquals(200, client.head("/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathAndConsumer(final boolean https, final ErsatzServer server) throws IOException {
        server.expects().HEAD("/something", req -> {
            req.secure(https).called(1);
            req.responds().code(200);
        });

        assertEquals(200, client.head("/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathMatcher(final boolean https, final ErsatzServer server) throws IOException {
        server.expects().HEAD(startsWith("/loader/")).secure(https).called(1).responds().code(200);

        assertEquals(200, client.head("/loader/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathMatcherAndConsumer(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.HEAD(startsWith("/loader/"), req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> res.code(200));
            });
        });

        assertEquals(200, client.head("/loader/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer (with response headers): https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathAndConsumerWithResponseHeaders(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(expect -> {
            expect.HEAD("/something", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.header("Alpha", "Header-A");
                    res.header("Bravo", List.of("Header-B1", "Header-B2"));
                    res.code(200);
                });
            });
        });

        val response = client.head("/something", https);
        assertEquals(200, response.code());
        assertEquals("Header-A", response.header("Alpha"));
        assertEquals(List.of("Header-B1", "Header-B2"), response.headers("Bravo"));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] BASIC authentication: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withBASICAuthentication(final boolean https, final ErsatzServer server) throws IOException {
        server.expectations(cfg -> {
            cfg.HEAD("/safe", req -> {
                basicAuth(req, "basicuser", "ba$icp@$$");
                req.secure(https);
                req.called(1);
                req.responder(res -> res.code(200));
            });
        });

        assertEquals(200, client.head("/safe", builder -> basicAuthHeader(builder, "basicuser", "ba$icp@$$"), https).code());
        verify(server);
    }
}
