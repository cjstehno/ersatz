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

import com.stehno.ersatz.util.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiThreadedUsageTest {

    private final ErsatzServer server = new ErsatzServer();
    private HttpClient client;

    @BeforeEach void beforeEach() {
        server.clearExpectations();
        client = new HttpClient();
    }

    @AfterEach void afterEach() {
        server.close();
    }

    @Test @DisplayName("Multiple concurrent calls") void multipleConcurrent() {
        final int requestCount = 8;

        server.expects().GET("/something").called(requestCount).responds().code(200);
        server.start();

        final var responses = new CopyOnWriteArrayList<Integer>();

        for (int r = 0; r < requestCount; r++) {
            client.getAsync(server.httpUrl("/something"))
                .thenAccept(res -> responses.add(res.code()));
        }

        await().until(() -> responses.size() == requestCount);

        assertTrue(server.verify());
        assertEquals(requestCount, responses.size());
        assertTrue(responses.stream().allMatch(r -> r == 200));
    }

    @Test @DisplayName("Multiple concurrent calls with listener") void multipleConcurrentWithListener() {
        final int requestCount = 8;
        final var counter = new AtomicInteger(0);

        server.expectations(e -> {
            e.GET("/something", req -> {
                req.called(requestCount);
                req.listener(cr -> counter.incrementAndGet());
                req.responder(res -> res.code(200));
            });
        });

        final var responses = new CopyOnWriteArrayList<Integer>();

        for (int r = 0; r < requestCount; r++) {
            client.getAsync(server.httpUrl("/something"))
                .thenAccept(res -> responses.add(res.code()));
        }

        await().until(() -> responses.size() == requestCount && counter.get() == requestCount);

        assertTrue(server.verify());
        assertEquals(requestCount, responses.size());
        assertTrue(responses.stream().allMatch(r -> r == 200));
    }
}