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
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static io.github.cjstehno.ersatz.match.PathMatcher.anyPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class})
class SharedErsatzServerExtensionTest {

    // Note: tests the case where no annotation is provided - it should still use the default-named config method

    @SuppressWarnings("unused") private static void serverConfig(final ServerConfig cfg) {
        cfg.requirements(requires -> {
            requires.that(GET, anyPath(), req -> {
                req.header("X-Foo", "BAR");
            });
        });
    }

    @Test void ensureConfig(final ErsatzServer server, final Client client) throws IOException {
        // if the default config is applied, the requirements will fail as expected
        server.expectations(expects -> {
            expects.GET("/hello", req -> req.called().responds().code(200).body("Hi!", TEXT_PLAIN));
        });

        val response = client.get("/hello");
        assertEquals(404, response.code());

        assertFalse(server.verify());
    }
}