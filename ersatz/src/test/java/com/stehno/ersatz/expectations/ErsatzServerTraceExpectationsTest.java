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
package com.stehno.ersatz.expectations;

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.cfg.ServerConfig;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.util.HttpClientExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Map;

import static com.stehno.ersatz.TestHelpers.resourceString;
import static com.stehno.ersatz.cfg.ContentType.MESSAGE_HTTP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class ErsatzServerTraceExpectationsTest {

    private final ErsatzServer server = new ErsatzServer(ServerConfig::https);
    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] tracing: https({0}) -> {1}")
    @MethodSource("com.stehno.ersatz.TestArguments#httpAndHttps")
    void traceSendsBackRequest(final boolean https) throws IOException {
        final var response = client.trace("/info?data=foo+bar", null, https);

        assertEquals(200, response.code());
        assertEquals(MESSAGE_HTTP.getValue(), response.header("Content-Type"));

        assertLinesMatch(
            resourceString("/trace.txt", Map.of(
                "<port>", String.valueOf(https ? server.getHttpsPort() : server.getHttpPort())
            )).lines(),
            response.body().string().lines()
        );
    }
}
