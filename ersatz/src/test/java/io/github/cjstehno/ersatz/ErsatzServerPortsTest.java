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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class})
@ApplyServerConfig("configure")
class ErsatzServerPortsTest {

    // NOTE: if this test starts failing for odd reasons, add some logic to ensure port is available

    @SuppressWarnings("unused") private Client client;

    @Test @DisplayName("running with explicit port")
    void explicitPort(final ErsatzServer ersatz) throws IOException {
        assertEquals(200, client.get("/hi").code());
        assertEquals(8675, ersatz.getHttpPort());
    }

    @SuppressWarnings("unused") private static void configure(final ServerConfig c) {
        c.httpPort(8675);
        c.autoStart(false);
        c.expectations(e -> e.GET("/hi").responds().code(200));
    }
}
