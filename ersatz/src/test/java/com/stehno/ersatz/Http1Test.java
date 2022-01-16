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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.LinkedList;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Http1Test {

    // Note: See https://github.com/cjstehno/ersatz/issues/125 for more details about this - http/1 is not currently
    // supported, though it "works".

    private ErsatzServer server = new ErsatzServer();

    @BeforeEach void beforeEach() {
        server.clearExpectations();
    }

    @AfterEach void afterEach() {
        server.close();
    }

    @Test @DisplayName("Support for Http/1")
    void http_1_support() throws Exception {
        server.expectations(exp -> {
            exp.GET("/hello", req -> {
                req.called(1);
//                req.header("Host", "localhost");
//                req.header("Accept", "application/json");
//                req.header("Accept-Charset", "UTF-8");
//                req.header("Accept-Encoding", "identity");
                req.responder(res -> {
                    res.code(200);
                    res.body("old-world", TEXT_PLAIN);
                });
            });
        });

        final var url = new URL(server.httpUrl("/hello"));
        final var connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);

        try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write("GET /containers/json HTTP/1.0\n");
            writer.write("Host: localhost\n");
            writer.write("Accept: application/json\n");
            writer.write("Accept-Charset: UTF-8\n");
            writer.write("Accept-Encoding: identity\n");
            writer.write("\n");
            writer.flush();
        }

        final var lines = new LinkedList<String>();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }

        final var response = String.join("\n", lines);
        assertEquals("old-world", response);

        server.verify();
    }
}
