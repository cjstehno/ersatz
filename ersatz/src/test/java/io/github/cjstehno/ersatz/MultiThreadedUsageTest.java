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

import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SharedErsatzServerExtension.class, HttpClientExtension.class})
@ApplyServerConfig("serverConfig")
class MultiThreadedUsageTest {

    private static final int REQUEST_COUNT = 4;
    @SuppressWarnings("unused") private Client client;

    @Test
    @DisplayName("Multiple concurrent calls")
    void multipleConcurrent(final ErsatzServer server) {
        server.expects().GET("/something").called(REQUEST_COUNT).responds().code(200);

        val responses = new CopyOnWriteArrayList<Integer>();

        for (int r = 0; r < REQUEST_COUNT; r++) {
            client.aget("/something").thenAccept(res -> responses.add(res.code()));
        }

        await().until(() -> responses.size() == REQUEST_COUNT);

        assertTrue(server.verify());
        assertEquals(REQUEST_COUNT, responses.size());
        assertTrue(responses.stream().allMatch(r -> r == 200));
    }

    @Test
    @DisplayName("Multiple concurrent calls with listener")
    void multipleConcurrentWithListener(final ErsatzServer server) {
        final var counter = new AtomicInteger(0);

        server.expectations(e -> e.GET("/something", req -> {
            req.called(REQUEST_COUNT);
            req.listener(cr -> counter.incrementAndGet());
            req.responder(res -> res.code(200));
        }));

        final var responses = new CopyOnWriteArrayList<Integer>();

        for (int r = 0; r < REQUEST_COUNT; r++) {
            client.aget("/something").thenAccept(res -> responses.add(res.code()));
        }

        await().until(() -> responses.size() == REQUEST_COUNT && counter.get() == REQUEST_COUNT);

        assertTrue(server.verify());
        assertEquals(REQUEST_COUNT, responses.size());
        assertTrue(responses.stream().allMatch(r -> r == 200));
    }

    @SuppressWarnings("unused")
    private static void serverConfig(final ServerConfig cfg) {
        cfg.serverThreads(1);
    }
}
