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
package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.ErsatzServer;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErsatzServerTest {

    @Test @DisplayName("not started should give useful error")
    void not_started() {
        val server = new ErsatzServer();
        try {

            final var thrown = assertThrows(IllegalStateException.class, () -> server.httpUrl("/nothing"));
            assertEquals("The port (-1) is invalid: Has the server been started?", thrown.getMessage());

        } finally {
            server.close();
        }
    }

    @Test @DisplayName("auto-start disabled")
    void autoStartDisabled() {
        val server = new ErsatzServer(cfg -> cfg.autoStart(false));
        try {

            server.expectations(expect -> {
                expect.GET("/foo").responds().code(200);
            });

            val thrown = assertThrows(Exception.class, server::getHttpUrl);
            assertEquals("The port (-1) is invalid: Has the server been started?", thrown.getMessage());

        } finally {
            server.close();
        }
    }

    @Test @DisplayName("auto-start enabled")
    void autoStartEnabled() {
        val server = new ErsatzServer(cfg -> cfg.autoStart(true));
        try {

            server.expectations(expect -> {
                expect.GET("/foo").responds().code(200);
            });

            assertTrue(server.getHttpUrl().startsWith("http://localhost:"));

        } finally {
            server.close();
        }
    }
}
