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
package io.github.cjstehno.ersatz.expectations;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
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

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class ErsatzServerDeleteExpectationsTest {

    private final ErsatzServer server = new ErsatzServer(ServerConfig::https);
    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPath(final boolean https) throws IOException {
        server.expectations(expect -> {
            expect.DELETE("/something").secure(https).called(1).responds().code(200);
        });

        assertEquals(200, client.delete("/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathAndConsumer(final boolean https) throws IOException {
        server.expects().DELETE("/something", req -> {
            req.secure(https).called(1);
            req.responds().code(200);
        });

        assertEquals(200, client.delete("/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcher(final boolean https, final String responseText) throws IOException {
        server.expects().DELETE(startsWith("/loader/")).secure(https).called(1).responds().code(200);

        assertEquals(200, client.delete("/loader/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcherAndConsumer(final boolean https, final String responseText) throws IOException {
        server.expectations(expect -> {
            expect.DELETE(startsWith("/loader/"), req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> res.code(200));
            });
        });

        assertEquals(200, client.delete("/loader/something", https).code());
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer (with response headers): https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withPathAndConsumerWithResponseHeaders(final boolean https) throws IOException {
        server.expectations(expect -> {
            expect.DELETE("/something", req -> {
                req.secure(https);
                req.called(1);
                req.responder(res -> {
                    res.header("Alpha", "Header-A");
                    res.header("Bravo", List.of("Header-B1", "Header-B2"));
                    res.code(200);
                });
            });
        });

        val response = client.delete("/something", https);
        assertEquals(200, response.code());
        assertEquals("Header-A", response.header("Alpha"));
        assertEquals(List.of("Header-B1", "Header-B2"), response.headers("Bravo"));

        verify(server);
    }

    @ParameterizedTest(name = "[{index}] BASIC authentication: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void withBASICAuthentication(final boolean https) throws IOException {
        server.expectations(cfg -> {
            cfg.DELETE("/safe", req -> {
                basicAuth(req, "basicuser", "ba$icp@$$");
                req.secure(https);
                req.called(1);
                req.responder(res -> res.code(200));
            });
        });

        assertEquals(200, client.delete("/safe", builder -> basicAuthHeader(builder, "basicuser", "ba$icp@$$"), https).code());
        verify(server);
    }
}
