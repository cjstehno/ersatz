/*
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
import com.stehno.ersatz.util.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ErsatzServerExtension.class)
class ErsatzServerPortsTest {

    // NOTE: if this test starts failing for odd reasons, add some logic to ensure port is available

    private HttpClient http;
    private ErsatzServer ersatz = new ErsatzServer(c -> {
        c.httpPort(8675);
        c.expectations(e -> e.GET("/hi").responds().code(200));
    });

    @BeforeEach void beforeEach() {
        http = new HttpClient();
    }

    @Test @DisplayName("running with explicit port")
    void explicitPort() throws IOException {
        assertNotNull(ersatz.start());

        assertEquals(200, http.get(ersatz.httpUrl("/hi")).code());
        assertEquals(8675, ersatz.getHttpPort());
    }
}
