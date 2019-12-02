/**
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.junit;

import com.stehno.ersatz.ErsatzServer;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErsatzServerRuleTest {

    @SuppressWarnings("unused") private ErsatzServer server;
    @Rule public ErsatzServerRule ersatzServerRule = new ErsatzServerRule(this);

    @Test
    public void using_server() throws IOException, InterruptedException {
        server.expectations(expects -> {
            expects.GET("/foo", request -> {
                request.called(1);
                request.responds().code(200);
            });
        });

        final HttpResponse<String> response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create(server.httpUrl("/foo"))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
    }
}