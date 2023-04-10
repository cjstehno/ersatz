/**
 * Copyright (C) 2023 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig
public class ErsatzServerProxyTest {

    // FIXME: this functionality needs to be in teh guide and feature list
    // FIXME: test against http/htts
    // FIXME: test: GET, HEAD, DELETE, POST, PUT, OPTIONS

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.decoder(APPLICATION_JSON, JsonEncDec.jsonDecoder);
        cfg.encoder(APPLICATION_JSON, Map.class, JsonEncDec.jsonEncoder);
        cfg.https();
    }

    @SuppressWarnings("unused") private Client client;

    @ParameterizedTest @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttps")
    void forwardGet(final boolean secure, final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerProxyTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.GET("/endpoint/get", req -> {
                    req.secure(secure);
                    req.called();
                    req.responder(res -> res.body(Map.of("status", "golden"), APPLICATION_JSON));
                });
            });

            server.expectations(expect -> {
                expect.GET("/endpoint/get", req -> {
                    req.secure(secure);
                    req.called();
                    req.forward(secure ? targetServer.getHttpsUrl() : targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.get("/endpoint/get", secure)) {
                assertEquals(200, responseFromPost.code());
                assertEquals("{\"status\":\"golden\"}", responseFromPost.body().string());
            }

            assertTrue(server.verify());
            assertTrue(targetServer.verify());
        }
    }

    @Test void usingErsatzPost(final ErsatzServer server) throws Exception {
        try (val targetServer = new ErsatzServer(ErsatzServerProxyTest::serverConfig)) {
            targetServer.expectations(expect -> {
                expect.POST("/endpoint/post", req -> {
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.responder(res -> {
                        res.body(Map.of("status", "golden"), APPLICATION_JSON);
                    });
                });
            });

            server.expectations(expect -> {
                expect.POST("/endpoint/post", req -> {
                    req.called();
                    req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                    req.forward(targetServer.getHttpUrl());
                });
            });

            try (val responseFromPost = client.post(
                "/endpoint/post",
                RequestBody.create("{\"foo\":\"bar\"}", MediaType.get("application/json"))
            )) {
                assertEquals(200, responseFromPost.code());
                assertEquals("{\"status\":\"golden\"}", responseFromPost.body().string());
            }

            assertTrue(server.verify());
        }
    }
}
