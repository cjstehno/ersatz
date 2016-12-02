/**
 * Copyright (C) 2016 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.stehno.ersatz.Verifiers.atLeast;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ErsatzServerTest {

    private final OkHttpClient client = new OkHttpClient();

    @Test
    public void prototype() throws IOException {
        final ErsatzServer ersatzServer = new ErsatzServer();

        final AtomicInteger counter = new AtomicInteger();
        final Consumer<GetRequest> listener = request -> counter.incrementAndGet();

        ersatzServer.requesting(expectations -> {
            expectations.get("/foo").verifier(atLeast(1))
                .responder(response -> response.body("This is Ersatz!!"))
                .responds().body("This is another response");

            expectations.get("/bar").verifier(atLeast(2)).listener(listener).responds().body("This is Bar!!");
            expectations.get("/baz").query("alpha", "42").responds().body("The answer is 42");
            expectations.get("/bing").header("bravo", "hello").responds().body("Heads up!").header("charlie", "goodbye").code(222);
        });

        ersatzServer.start();

        Request request = new Request.Builder().url("http://localhost:8080/foo").build();
        assertEquals("This is Ersatz!!", client.newCall(request).execute().body().string());

        request = new Request.Builder().url("http://localhost:8080/foo").build();
        assertEquals("This is another response", client.newCall(request).execute().body().string());

        request = new Request.Builder().url("http://localhost:8080/bar").build();
        assertEquals("This is Bar!!", client.newCall(request).execute().body().string());
        assertEquals("This is Bar!!", client.newCall(request).execute().body().string());
        assertEquals(2, counter.get());

        request = new Request.Builder().url("http://localhost:8080/baz?alpha=42").build();
        assertEquals("The answer is 42", client.newCall(request).execute().body().string());

        request = new Request.Builder().url("http://localhost:8080/bing").addHeader("bravo", "hello").build();
        okhttp3.Response resp = client.newCall(request).execute();
        assertEquals(222, resp.code());
        assertEquals("goodbye", resp.header("charlie"));
        assertEquals("Heads up!", resp.body().string());

        assertTrue(ersatzServer.verify());
    }
}
