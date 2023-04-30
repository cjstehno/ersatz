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
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ErsatzServerExtension.class)
class ErsatzServerTest {

    @Test @DisplayName("verify url information") @ApplyServerConfig("infoConfig")
    void urlInformation(final ErsatzServer server) {
        assertEquals("http://localhost:8182", server.getHttpUrl());
        assertEquals("http://localhost:8182", server.getUrl(false));
        assertEquals("http://localhost:8182/stuff", server.httpUrl("/stuff"));

        assertEquals("https://localhost:8584", server.getHttpsUrl());
        assertEquals("https://localhost:8584", server.getUrl(true));
        assertEquals("https://localhost:8584/stuff", server.httpsUrl("/stuff"));
    }

    @SuppressWarnings("unused") private void infoConfig(final ServerConfig cfg) {
        cfg.reportToConsole();
        cfg.https();
        cfg.httpPort(8182);
        cfg.httpsPort(8584);
        cfg.serverThreads(1);
        cfg.timeout(5);
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
