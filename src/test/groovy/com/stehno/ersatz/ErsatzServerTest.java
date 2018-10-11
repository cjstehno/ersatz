/**
 * Copyright (C) 2018 Christopher J. Stehno
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

import com.stehno.ersatz.junit.ErsatzServerRule;
import com.stehno.ersatz.util.HttpClient;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.stehno.ersatz.ContentType.TEXT_PLAIN;
import static com.stehno.ersatz.Decoders.getUtf8String;
import static java.util.Collections.singletonMap;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ErsatzServerTest {

    @Rule
    public ErsatzServerRule ersatzServer = new ErsatzServerRule();
    private final HttpClient http = new HttpClient();

    @Test
    public void prototype() throws IOException {
        final AtomicInteger counter = new AtomicInteger();
        final Consumer<ClientRequest> listener = request -> counter.incrementAndGet();

        ersatzServer.expectations(expectations -> {
            expectations.get("/foo").called(greaterThanOrEqualTo(1))
                .responder(response -> response.body("This is Ersatz!!"))
                .responds().body("This is another response");

            expectations.get("/bar").called(greaterThanOrEqualTo(2)).listener(listener).responds().body("This is Bar!!");

            expectations.get("/baz").query("alpha", "42").responds().body("The answer is 42");

            expectations.get("/bing").header("bravo", "hello").responds().body("Heads up!").header("charlie", "goodbye").code(222);

            expectations.get("/cookie/monster").cookie("flavor", "chocolate-chip").responds().body("I love cookies!").cookie("eaten", "yes");

            expectations.head("/head").responds().header("foo", "blah").code(123);

            expectations.post("/form").body("some content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, getUtf8String())
                .responds().body("response");

            expectations.put("/update").body("more content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, getUtf8String())
                .responds().body("updated");

            expectations.delete("/remove").responds().body("removed");

            expectations.post("/patch").body("a change", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, getUtf8String())
                .responds().body("patched");
        });

        assertEquals("This is Ersatz!!", http.get(ersatzServer.httpUrl("/foo")).body().string());

        assertEquals("This is another response", http.get(ersatzServer.httpUrl("/foo")).body().string());

        assertEquals("This is Bar!!", http.get(ersatzServer.httpUrl("/bar")).body().string());
        assertEquals("This is Bar!!", http.get(ersatzServer.httpUrl("/bar")).body().string());
        assertEquals(2, counter.get());

        assertEquals("The answer is 42", http.get(ersatzServer.httpUrl("/baz?alpha=42")).body().string());

        okhttp3.Response resp = http.get(singletonMap("bravo", "hello"), ersatzServer.httpUrl("/bing"));
        assertEquals(222, resp.code());
        assertEquals("goodbye", resp.header("charlie"));
        assertEquals("Heads up!", resp.body().string());

        resp = http.get(singletonMap("Cookie", "flavor=chocolate-chip"), ersatzServer.httpUrl("/cookie/monster"));
        assertEquals("I love cookies!", resp.body().string());
        assertEquals("eaten=yes", resp.header("Set-Cookie"));

        resp = http.head(ersatzServer.httpUrl("/head"));
        assertEquals(123, resp.code());
        assertEquals("blah", resp.header("foo"));

        resp = http.post(ersatzServer.httpUrl("/form"), create(parse("text/plain"), "some content"));
        assertEquals("response", resp.body().string());

        resp = http.put(ersatzServer.httpUrl("/update"), create(parse("text/plain"), "more content"));
        assertEquals("updated", resp.body().string());

        resp = http.delete(ersatzServer.httpUrl("/remove"));
        assertEquals("removed", resp.body().string());

        resp = http.post(ersatzServer.httpUrl("/patch"), create(parse("text/plain"), "a change"));
        assertEquals("patched", resp.body().string());

        assertTrue(ersatzServer.verify());
    }

    @Test
    public void alternateConfiguration() throws IOException {
        ErsatzServer server = new ErsatzServer(config -> {
            config.expects().get(startsWith("/hello")).responds().body("ok");
        });
        server.start();

        assertEquals("ok", http.get(server.getHttpUrl() + "/hello/there").body().string());

        server.stop();
    }

    @After
    public void after() {
        ersatzServer.stop();
    }
}
