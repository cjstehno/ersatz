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
package com.stehno.ersatz;

import com.stehno.ersatz.util.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static com.stehno.ersatz.encdec.Decoders.utf8String;
import static java.util.Collections.singletonMap;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class ErsatzServerTest {

    private ErsatzServer ersatzServer = new ErsatzServer();
    private final HttpClient http = new HttpClient();

    @BeforeEach void beforeEach() {
        ersatzServer.clearExpectations();
    }

    @AfterEach void afterEach() {
        ersatzServer.close();
    }

    @Test void prototype() throws IOException {
        final AtomicInteger counter = new AtomicInteger();
        final Consumer<ClientRequest> listener = request -> counter.incrementAndGet();

        ersatzServer.expectations(expectations -> {
            expectations.GET("/foo").called(greaterThanOrEqualTo(1))
                .responder(response -> response.body("This is Ersatz!!"))
                .responds().body("This is another response");

            expectations.GET("/bar").called(greaterThanOrEqualTo(2)).listener(listener).responds().body("This is Bar!!");

            expectations.GET("/baz").query("alpha", "42").responds().body("The answer is 42");

            expectations.GET("/bing").header("bravo", "hello").responds().body("Heads up!").header("charlie", "goodbye").code(222);

            expectations.GET("/cookie/monster").cookie("flavor", "chocolate-chip").responds().body("I love cookies!").cookie("eaten", "yes");

            expectations.HEAD("/head").responds().header("foo", "blah").code(123);

            expectations.POST("/form").body("some content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, utf8String)
                .responds().body("response");

            expectations.PUT("/update").body("more content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, utf8String)
                .responds().body("updated");

            expectations.DELETE("/remove").responds().body("removed");

            expectations.POST("/patch").body("a change", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, utf8String)
                .responds().body("patched");
        });

        assertEquals("This is Ersatz!!", http.get(ersatzServer.httpUrl("/foo")).body().string());

        assertEquals("This is another response", http.get(ersatzServer.httpUrl("/foo")).body().string());

        assertEquals("This is Bar!!", http.get(ersatzServer.httpUrl("/bar")).body().string());
        assertEquals("This is Bar!!", http.get(ersatzServer.httpUrl("/bar")).body().string());

        await().untilAtomic(counter, equalTo(2));

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

    @Test void alternateConfiguration() throws IOException {
        ErsatzServer server = new ErsatzServer(config -> {
            config.expects().GET(startsWith("/hello")).responds().body("ok");
        });
        server.start();

        assertEquals("ok", http.get(server.getHttpUrl() + "/hello/there").body().string());

        server.stop();
    }
}
