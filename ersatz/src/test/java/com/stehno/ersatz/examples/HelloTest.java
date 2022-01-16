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
package com.stehno.ersatz.examples;

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ErsatzServerExtension.class)
class HelloTest {

    private ErsatzServer server;

    @Test void sayHello() throws Exception {
        server.expectations(expect -> {
            expect.GET("/say/hello", req -> {
                req.called(1);
                req.query("name", "Ersatz");
                req.responder(res -> {
                    res.body("Hello, Ersatz", TEXT_PLAIN);
                });
            });
        });

        final var request = HttpRequest
            .newBuilder(new URI(server.httpUrl("/say/hello?name=Ersatz")))
            .GET()
            .build();

        final var response = newHttpClient().send(request, ofString());

        assertEquals(200, response.statusCode());
        assertEquals("Hello, Ersatz", response.body());
        assertTrue(server.verify());
    }
}