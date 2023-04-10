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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.encdec.DecodingContext;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class}) @ApplyServerConfig
public class ErsatzServerProxyTest {

    // FIXME: there seems to be an error using Ersatz as the target - not fully consuming request?

    private static final BiFunction<byte[], DecodingContext, Object> jsonDecoder = (content, ctx) -> {
        try {
            return new ObjectMapper().readValue(content != null ? content : "{}".getBytes(UTF_8), Map.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    };

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.decoder(APPLICATION_JSON, jsonDecoder);
    }

    @SuppressWarnings("unused") private Client client;

    @Test void postRequest(final ErsatzServer server) throws Exception {
        server.expectations(expect -> {
            expect.POST("/endpoint/post", req -> {
                req.called();
                req.body(Map.of("foo", "bar"), APPLICATION_JSON);
                req.forward("http://localhost:8080");
            });
        });

        try (val responseFromPost = client.post(
            "/endpoint/post",
            RequestBody.create("{\"foo\":\"bar\"}", MediaType.get("application/json"))
        )) {
            assertEquals(200, responseFromPost.code());
            assertEquals("{\"status\":\"good\"}", responseFromPost.body().string());
        }

        assertTrue(server.verify());
    }

    @Test void getRequest(final ErsatzServer server) throws Exception {
        server.expectations(expect -> {
            expect.GET("/endpoint/get", req -> {
                req.called();
                req.forward("http://localhost:8080");
            });
        });

        try (val responseFromPost = client.get("/endpoint/get")) {
            assertEquals(200, responseFromPost.code());
            assertEquals("{\"status\":\"good\"}", responseFromPost.body().string());
        }

        assertTrue(server.verify());
    }
}
