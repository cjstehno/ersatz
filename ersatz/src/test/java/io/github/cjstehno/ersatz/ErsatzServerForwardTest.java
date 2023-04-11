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
package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import io.github.cjstehno.ersatz.util.JsonEncDec;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig
public class ErsatzServerForwardTest {

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.decoder(APPLICATION_JSON, JsonEncDec.jsonDecoder);
        cfg.encoder(APPLICATION_JSON, Map.class, JsonEncDec.jsonEncoder);
        cfg.https();
    }

    @SuppressWarnings("unused") private Client client;

    @ParameterizedTest @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void forwardGet(final boolean secure, final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerForwardTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.GET("/endpoint/get", req -> {
                    req.secure(secure);
                    req.called();
                    req.query("foo", "bar");
                    req.responder(res -> res.body(Map.of("status", "golden"), APPLICATION_JSON));
                });
            });

            server.expectations(expect -> {
                expect.GET("/endpoint/get", req -> {
                    req.secure(secure);
                    req.called();
                    req.query("foo", "bar");
                    req.forward(secure ? targetServer.getHttpsUrl() : targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.get("/endpoint/get?foo=bar", secure)) {
                assertEquals(200, responseFromPost.code());
                assertEquals("{\"status\":\"golden\"}", responseFromPost.body().string());
            }

            assertTrue(server.verify());
            assertTrue(targetServer.verify());
        }
    }

    @ParameterizedTest @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void forwardHead(final boolean secure, final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerForwardTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.HEAD("/endpoint/head", req -> {
                    req.secure(secure);
                    req.called();
                    req.responds().code(201);
                });
            });

            server.expectations(expect -> {
                expect.HEAD("/endpoint/head", req -> {
                    req.secure(secure);
                    req.called();
                    req.forward(secure ? targetServer.getHttpsUrl() : targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.head("/endpoint/head", secure)) {
                assertEquals(201, responseFromPost.code());
            }

            assertTrue(server.verify());
            assertTrue(targetServer.verify());
        }
    }

    @ParameterizedTest @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void forwardDelete(final boolean secure, final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerForwardTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.DELETE("/endpoint/delete", req -> {
                    req.secure(secure);
                    req.called();
                    req.responds().code(201);
                });
            });

            server.expectations(expect -> {
                expect.DELETE("/endpoint/delete", req -> {
                    req.secure(secure);
                    req.called();
                    req.forward(secure ? targetServer.getHttpsUrl() : targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.delete("/endpoint/delete", secure)) {
                assertEquals(201, responseFromPost.code());
            }

            assertTrue(server.verify());
            assertTrue(targetServer.verify());
        }
    }

    @ParameterizedTest @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void usingErsatzPost(final boolean secure, final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerForwardTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.POST("/endpoint/post", req -> {
                    req.secure(secure);
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.responder(res -> res.body(Map.of("status", "golden"), APPLICATION_JSON));
                });
            });

            server.expectations(expect -> {
                expect.POST("/endpoint/post", req -> {
                    req.secure(secure);
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.forward(secure ? targetServer.getHttpsUrl() : targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.post(
                "/endpoint/post",
                RequestBody.create("{\"foo\":\"bar\"}", MediaType.get("application/json")),
                secure
            )) {
                assertEquals(200, responseFromPost.code());
                assertEquals("{\"status\":\"golden\"}", responseFromPost.body().string());
            }

            assertTrue(server.verify());
        }
    }

    @ParameterizedTest @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void usingErsatzPut(final boolean secure, final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerForwardTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.PUT("/endpoint/put", req -> {
                    req.secure(secure);
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.responds().code(201);
                });
            });

            server.expectations(expect -> {
                expect.PUT("/endpoint/put", req -> {
                    req.secure(secure);
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.forward(secure ? targetServer.getHttpsUrl() : targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.put(
                "/endpoint/put",
                RequestBody.create("{\"foo\":\"bar\"}", MediaType.get("application/json")),
                secure
            )) {
                assertEquals(201, responseFromPost.code());
            }

            assertTrue(server.verify());
        }
    }

    @ParameterizedTest @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void usingErsatzPatch(final boolean secure, final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerForwardTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.PATCH("/endpoint/patch", req -> {
                    req.secure(secure);
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.responds().code(201);
                });
            });

            server.expectations(expect -> {
                expect.PATCH("/endpoint/patch", req -> {
                    req.secure(secure);
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.forward(secure ? targetServer.getHttpsUrl() : targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.patch(
                "/endpoint/patch",
                RequestBody.create("{\"foo\":\"bar\"}", MediaType.get("application/json")),
                secure
            )) {
                assertEquals(201, responseFromPost.code());
            }

            assertTrue(server.verify());
        }
    }
}
