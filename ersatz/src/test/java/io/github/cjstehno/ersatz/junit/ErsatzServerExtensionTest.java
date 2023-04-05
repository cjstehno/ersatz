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
package io.github.cjstehno.ersatz.junit;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.match.PathMatcher;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;

import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ErsatzServerExtension.class)
class ErsatzServerExtensionTest {

    @SuppressWarnings("unused") private ErsatzServer server;

    // FIXME: make a test without a field

    @Test @DisplayName("using provided server")
    void usingServer() throws Exception {
        server.expectations(expects -> {
            expects.GET("/foo", request -> {
                request.called(1);
                request.responds().code(200);
            });
        });

        val response = newHttpClient().send(
            newBuilder(URI.create(server.httpUrl("/foo"))).GET().build(),
            ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(server.verify());
    }

    @Test @DisplayName("Using annotation config with field")
    @ApplyServerConfig("localConfig")
    void usingAnnotationConfigWithField() throws Exception {
        server.expectations(expects -> {
            expects.GET("/foo", request -> {
                request.called(1);
                request.responds().code(200);
            });
        });

        assertEquals(
            200,
            newHttpClient().send(
                newBuilder(URI.create(server.httpUrl("/foo"))).GET().header("key", "unlocked").build(),
                ofString()
            ).statusCode()
        );
        assertEquals(
            404,
            newHttpClient().send(
                newBuilder(URI.create(server.httpUrl("/foo"))).GET().build(),
                ofString()
            ).statusCode()
        );
        assertTrue(server.verify());
    }

    @Test @DisplayName("Using annotation config with field and param")
    @ApplyServerConfig("localConfig")
    void usingAnnotationConfigWithFieldAndParam(final ErsatzServer myServer) throws Exception {
        myServer.expectations(expects -> {
            expects.GET("/foo", request -> {
                request.called(1);
                request.responds().code(200);
            });
        });

        assertEquals(
            200,
            newHttpClient().send(
                newBuilder(URI.create(myServer.httpUrl("/foo"))).GET().header("key", "unlocked").build(),
                ofString()
            ).statusCode()
        );
        assertEquals(
            404,
            newHttpClient().send(
                newBuilder(URI.create(myServer.httpUrl("/foo"))).GET().build(),
                ofString()
            ).statusCode()
        );
        assertTrue(myServer.verify());
    }

    private void localConfig(final ServerConfig config) {
        config.requirements(requires -> {
            requires.that(HttpMethod.GET, pathMatching("/foo"), cfg -> {
                cfg.header("key", "unlocked");
            });
        });
    }
}