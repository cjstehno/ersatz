/**
 * Copyright (C) 2017 Christopher J. Stehno
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
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.stehno.ersatz.ContentType.TEXT_PLAIN;
import static com.stehno.ersatz.Decoders.getUtf8String;
import static java.lang.String.format;
import static okhttp3.MediaType.parse;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ErsatzServerTest {

    @Rule
    public ErsatzServerRule ersatzServer = new ErsatzServerRule();

    private OkHttpClient client;

    @Before
    public void before() {
        client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build();
    }

    @Test
    public void prototype() throws IOException {
        final AtomicInteger counter = new AtomicInteger();
        final Consumer<ClientRequest> listener = request -> counter.incrementAndGet();

        ersatzServer.expectations(expectations -> {
            expectations.get("/foo").called(greaterThanOrEqualTo(1))
                .responder(response -> response.content("This is Ersatz!!"))
                .responds().content("This is another response");

            expectations.get("/bar").called(greaterThanOrEqualTo(2)).listener(listener).responds().content("This is Bar!!");

            expectations.get("/baz").query("alpha", "42").responds().content("The answer is 42");

            expectations.get("/bing").header("bravo", "hello").responds().content("Heads up!").header("charlie", "goodbye").code(222);

            expectations.get("/cookie/monster").cookie("flavor", "chocolate-chip").responds().content("I love cookies!").cookie("eaten", "yes");

            expectations.head("/head").responds().header("foo", "blah").code(123);

            expectations.post("/form").body("some content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, getUtf8String())
                .responds().content("response");

            expectations.put("/update").body("more content", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, getUtf8String())
                .responds().content("updated");

            expectations.delete("/remove").responds().content("removed");

            expectations.post("/patch").body("a change", "text/plain; charset=utf-8").decoder(TEXT_PLAIN, getUtf8String())
                .responds().content("patched");
        });

        okhttp3.Request request = new okhttp3.Request.Builder().url(url("/foo")).build();
        assertEquals("This is Ersatz!!", client.newCall(request).execute().body().string());

        request = new okhttp3.Request.Builder().url(url("/foo")).build();
        assertEquals("This is another response", client.newCall(request).execute().body().string());

        request = new okhttp3.Request.Builder().url(url("/bar")).build();
        assertEquals("This is Bar!!", client.newCall(request).execute().body().string());
        assertEquals("This is Bar!!", client.newCall(request).execute().body().string());
        assertEquals(2, counter.get());

        request = new okhttp3.Request.Builder().url(url("/baz?alpha=42")).build();
        assertEquals("The answer is 42", client.newCall(request).execute().body().string());

        request = new okhttp3.Request.Builder().url(url("/bing")).addHeader("bravo", "hello").build();
        okhttp3.Response resp = client.newCall(request).execute();
        assertEquals(222, resp.code());
        assertEquals("goodbye", resp.header("charlie"));
        assertEquals("Heads up!", resp.body().string());

        request = new okhttp3.Request.Builder().url(url("/cookie/monster")).addHeader("Cookie", "flavor=chocolate-chip").build();
        resp = client.newCall(request).execute();
        assertEquals("I love cookies!", resp.body().string());
        assertEquals("eaten=yes", resp.header("Set-Cookie"));

        resp = client.newCall(new okhttp3.Request.Builder().head().url(url("/head")).build()).execute();
        assertEquals(123, resp.code());
        assertEquals("blah", resp.header("foo"));

        request = new okhttp3.Request.Builder().post(RequestBody.create(parse("text/plain"), "some content")).url(url("/form")).build();
        resp = client.newCall(request).execute();
        assertEquals("response", resp.body().string());

        request = new okhttp3.Request.Builder().put(RequestBody.create(parse("text/plain"), "more content")).url(url("/update")).build();
        resp = client.newCall(request).execute();
        assertEquals("updated", resp.body().string());

        request = new okhttp3.Request.Builder().delete().url(url("/remove")).build();
        resp = client.newCall(request).execute();
        assertEquals("removed", resp.body().string());

        request = new okhttp3.Request.Builder().post(RequestBody.create(parse("text/plain"), "a change")).url(url("/patch")).build();
        resp = client.newCall(request).execute();
        assertEquals("patched", resp.body().string());

        assertTrue(ersatzServer.verify());
    }

    @Test
    public void alternateConfiguration() throws IOException {
        ErsatzServer server = new ErsatzServer(config -> {
            config.expects().get(startsWith("/hello")).responds().content("ok");
        });
        server.start();

        okhttp3.Request request = new okhttp3.Request.Builder().get().url(server.getHttpUrl() + "/hello/there").build();
        assertEquals("ok", client.newCall(request).execute().body().string());

        server.stop();
    }

    private String url(final String path) {
        return format("http://localhost:%d%s", ersatzServer.getHttpPort(), path);
    }

    @After
    public void after() {
        ersatzServer.stop();
    }
}
