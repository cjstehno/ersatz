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

import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SharedErsatzServerExtension.class)
class ErsatzProxyServerTest {

    @Test void usage(final ErsatzServer server) throws Exception {
        val proxyServer = new ErsatzProxyServer(config -> {
            config.target(server.getHttpUrl()).expectations(expects -> {
                expects.get("/foo");
            });
        });

        server.expectations(expects -> {
            expects.GET("/foo", res -> res.responds().code(200));
        });

        val client = new OkHttpClient.Builder().build();
        val request = new Request.Builder().method("GET", null).url(proxyServer.getUrl() + "/foo").build();
        try (val response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
        }

        server.verify();
        proxyServer.verify();

        proxyServer.close();
    }
}