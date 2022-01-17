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

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.encdec.Encoders;
import com.stehno.ersatz.encdec.MultipartResponseContent;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.util.HttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.stehno.ersatz.cfg.ContentType.MESSAGE_HTTP;
import static com.stehno.ersatz.cfg.ContentType.MULTIPART_MIXED;
import static com.stehno.ersatz.cfg.HttpMethod.DELETE;
import static com.stehno.ersatz.cfg.HttpMethod.GET;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(ErsatzServerExtension.class)
class AnotherErsatzServerTest {

    // FIXME: refactor this and the other one into more contextual test suites

    private ErsatzServer ersatzServer = new ErsatzServer(c -> {
        c.encoder(MULTIPART_MIXED, MultipartResponseContent.class, Encoders.multipart);
    });
    private HttpClient http;

    @BeforeEach void beforeEach() {
        http = new HttpClient();
    }

    @ParameterizedTest @DisplayName("OPTIONS #path allows #allowed") @MethodSource("optionsProvider")
    void optionsPathAllows(String path, Collection<String> allowed) throws IOException {
        ersatzServer.expectations(e -> {
            e.OPTIONS("/options").responds().allows(GET, HttpMethod.POST).code(200);
            e.OPTIONS("/*").responds().allows(DELETE, GET, HttpMethod.OPTIONS).code(200);
        });

        final var response = http.options(ersatzServer.httpUrl(path));

        assertEquals(200, response.code());
        assertEquals(allowed.size(), response.headers("Allow").size());
        assertTrue(response.headers("Allow").containsAll(allowed));
        assertArrayEquals(new byte[0], response.body().bytes());
    }

    private static Stream<Arguments> optionsProvider() {
        return Stream.of(
            arguments("/options", List.of("GET", "POST")),
            arguments("/*", List.of("OPTIONS", "GET", "DELETE"))
        );
    }

    @Test @DisplayName("TRACE sends back request") void traceSendsBackRequest() throws IOException {
        ersatzServer.start();

        final var response = http.trace(ersatzServer.httpUrl("/info?data=foo+bar"));

        assertEquals(MESSAGE_HTTP.getValue(), response.body().contentType().toString());
        assertEquals(200, response.code());

        final var expected = IOUtils.toString(AnotherErsatzServerTest.class.getResourceAsStream("/trace.txt"))
            .replace("{port}", String.valueOf(ersatzServer.getHttpPort()));

        final var expectedLines = expected.lines().collect(toList());
        final var actualLines = response.body().string().lines().collect(toList());

        assertLinesMatch(expectedLines, actualLines);
    }
}

