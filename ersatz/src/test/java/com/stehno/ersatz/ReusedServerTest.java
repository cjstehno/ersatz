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

import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.util.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ErsatzServerExtension.class)
class ReusedServerTest {

    private ErsatzServer ersatzServer;
    private HttpClient client;

    @BeforeEach void beforeEach() {
        client = new HttpClient();

        ersatzServer.expectations(e -> {
            e.GET("/alpha").called(1).responds().body("alpha-response", TEXT_PLAIN);
            e.GET("/bravo").called(2).responds().body("bravo-response", TEXT_PLAIN);
        });
    }

    @Test @DisplayName("expected calls") void expectedCalls() throws IOException {
        String resp1 = request("/alpha");
        String resp2 = request("/bravo");
        String resp3 = request("/bravo");

        assertEquals("alpha-response", resp1);
        assertEquals("bravo-response", resp2);
        assertEquals("bravo-response", resp3);
        assertTrue(ersatzServer.verify());
    }

    @Test @DisplayName("clear expectations and they should be not-found")
    void clearShouldNotBeFound() throws IOException {
        ersatzServer.clearExpectations();

        assertEquals("404: Not Found", request("/alpha"));
        assertEquals("404: Not Found", request("/bravo"));
    }

    @Test @DisplayName("clear expectations and add new ones") void clearAndAdd() throws IOException {
        ersatzServer.clearExpectations();

        ersatzServer.expectations(e -> {
            e.GET("/charlie").called(1).responds().body("charlie-response", TEXT_PLAIN);
        });

        assertEquals("charlie-response", request("/charlie"));
    }

    @Test @DisplayName("same calls again to ensure that server resets normally")
    void sameCallsAgain() throws IOException {
        String resp1 = request("/alpha");
        String resp2 = request("/bravo");
        String resp3 = request("/bravo");

        assertEquals("alpha-response", resp1);
        assertEquals("bravo-response", resp2);
        assertEquals("bravo-response", resp3);
        assertTrue(ersatzServer.verify());
    }

    private String request(final String path) throws IOException {
        return client.get(ersatzServer.httpUrl(path)).body().string();
    }
}
