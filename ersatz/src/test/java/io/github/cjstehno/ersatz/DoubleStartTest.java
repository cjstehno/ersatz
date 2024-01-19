/**
 * Copyright (C) 2024 Christopher J. Stehno
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
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test is to verify issue <a href="https://github.com/cjstehno/ersatz/issues/182">#182</a>.
 */
@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
@ApplyServerConfig
public class DoubleStartTest {

    @SuppressWarnings("unused") private void serverConfig(final ServerConfig cfg) {
        cfg.expectations(expects -> {
            expects.GET("/init", req -> {
                req.called();
                req.responds().code(200);
            });
        });
    }

    @Test void moreExpectations(final ErsatzServer server, final HttpClientExtension.Client http) throws IOException {
        server.expectations(expects -> {
            expects.GET("/stuff", req -> {
                req.called().responds().code(200).body("things", TEXT_PLAIN);
            });
        });

        assertEquals(200, http.get("/init").code());

        val stuffResponse = http.get("/stuff");
        assertEquals(200, stuffResponse.code());
        assertEquals("things", stuffResponse.body().string());

        server.assertVerified();
    }

    @Test void directConfig() throws Exception{
        try (val server = new ErsatzServer(cfg -> {
            // setup some server expectations
            cfg.expectations(expects -> {
                expects.GET("/setup", req -> {
                    req.called();
                    req.responds().code(200);
                });
            });
        })) {
            // setup more expectations
            server.expectations(expects -> {
                expects.GET("/other", req -> {
                    req.called().responds().code(200).body("things", TEXT_PLAIN);
                });
            });

            val http = new HttpClientExtension.Client(server.getHttpUrl(), null, false);

            assertEquals(200, http.get("/setup").code());

            val stuffResponse = http.get("/other");
            assertEquals(200, stuffResponse.code());
            assertEquals("things", stuffResponse.body().string());

            server.assertVerified();
        }
    }
}
