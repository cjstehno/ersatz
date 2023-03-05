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
package io.github.cjstehno.ersatz.expectations;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class ErsatzServerOptionsExpectationsTest {

    private final ErsatzServer server = new ErsatzServer(ServerConfig::https);
    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] allowed options: https({0}) {1} -> {2}")
    @MethodSource("optionsProvider")
    void optionsPathAllows(final boolean https, final String path,final Collection<String> allowed) throws IOException {
        server.expectations(expect -> {
            expect.OPTIONS("/options").secure(https).responds().allows(GET, POST).code(200);
            expect.OPTIONS("/*").secure(https).responds().allows(DELETE, GET, OPTIONS).code(200);
        });

        val response = client.options(path, null, https);

        assertEquals(200, response.code());
        assertEquals(allowed.size(), response.headers("Allow").size());
        assertTrue(response.headers("Allow").containsAll(allowed));
        assertArrayEquals(new byte[0], response.body().bytes());
    }

    private static Stream<Arguments> optionsProvider() {
        return Stream.of(
            arguments(false, "/options", List.of("GET", "POST")),
            arguments(false, "/*", List.of("OPTIONS", "GET", "DELETE")),

            arguments(true, "/options", List.of("GET", "POST")),
            arguments(true, "/*", List.of("OPTIONS", "GET", "DELETE"))
        );
    }
}
