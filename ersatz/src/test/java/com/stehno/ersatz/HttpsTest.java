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
package com.stehno.ersatz;

import com.stehno.ersatz.cfg.ServerConfig;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.util.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ErsatzServerExtension.class)
class HttpsTest {

    private ErsatzServer server = new ErsatzServer(ServerConfig::https);
    private HttpClient http;

    @BeforeEach void beforeEach() {
        http = new HttpClient(true);
    }

    @Test @DisplayName("Https server") void httpsServer() throws IOException {
        server.expectations(e -> {
            e.GET("/hello").protocol("https").responds().body("This is HTTPS!");
        });

        final var response = http.get(server.httpsUrl("/hello"));
        assertEquals("This is HTTPS!", response.body().string());
    }
}