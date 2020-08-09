/**
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz;

import com.stehno.ersatz.junit.ErsatzServerExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ErsatzServerExtension.class)
class ErsatzProxyTest {

    // FIXME: need to test closure

    @SuppressWarnings("unused") private ErsatzServer ersatz;

    @Test @DisplayName("configuration with consumer")
    void config_consumer() throws Exception {
        ersatz.expectations(exp -> {
            exp.GET("/", req -> {
                req.called(1);
                req.responder(res -> {
                    res.code(200);
                    res.body("Hello", TEXT_PLAIN);
                });
            });
            exp.GET("/foo", req -> {
                req.called(1);
                req.responder(res -> {
                    res.code(200);
                    res.body("Foo!", TEXT_PLAIN);
                });
            });
        });

        final var proxy = new ErsatzProxy(cfg -> {
            cfg.target(ersatz.getHttpUrl());
            cfg.expectations(exp -> {
                exp.GET("/");
                exp.GET("/foo");
            });
        });

        final var client = HttpClient.newHttpClient();

         var response = client.send(newBuilder(new URI(proxy.getUrl())).GET().build(), ofString());
        assertEquals("Hello", response.body());

        response = client.send(newBuilder(new URI(proxy.getUrl() + "/foo")).GET().build(), ofString());
        assertEquals("Foo!", response.body());

        assertTrue(ersatz.verify());
        assertTrue(proxy.verify());
    }
}
