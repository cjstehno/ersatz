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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ErsatzServerTest {

    private final OkHttpClient client = new OkHttpClient();

    @Test
    public void prototype() throws IOException {
        final ErsatzServer ersatzServer = new ErsatzServer();

        ersatzServer.requesting(expectations -> {
            expectations.get("/foo").atLeast(1).responds().body("This is Ersatz!!");
            expectations.get("/bar").atLeast(2).responds().body("This is Bar!!");
            expectations.get("/baz").query("alpha", "42").responds().body("The answer is 42");
            expectations.get("/bing").header("bravo", "hello").responds().body("Heads up!");
        });

        ersatzServer.start();

        Request request = new Request.Builder().url("http://localhost:8080/foo").build();
        assertEquals("This is Ersatz!!", client.newCall(request).execute().body().string());

        request = new Request.Builder().url("http://localhost:8080/bar").build();
        assertEquals("This is Bar!!", client.newCall(request).execute().body().string());
        assertEquals("This is Bar!!", client.newCall(request).execute().body().string());

        request = new Request.Builder().url("http://localhost:8080/baz?alpha=42").build();
        assertEquals("The answer is 42", client.newCall(request).execute().body().string());

        request = new Request.Builder().url("http://localhost:8080/bing").addHeader().build();
        assertEquals("The answer is 42", client.newCall(request).execute().body().string());

        assertTrue(ersatzServer.verify());
    }
}
