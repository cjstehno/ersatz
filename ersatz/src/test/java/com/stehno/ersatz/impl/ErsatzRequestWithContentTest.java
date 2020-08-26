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
package com.stehno.ersatz.impl;


import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.cfg.ContentType;
import com.stehno.ersatz.encdec.Decoders;
import com.stehno.ersatz.encdec.DecodingContext;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.server.MockClientRequest;
import com.stehno.ersatz.util.HttpClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

import static com.stehno.ersatz.cfg.ContentType.*;
import static com.stehno.ersatz.cfg.HttpMethod.POST;
import static com.stehno.ersatz.encdec.MultipartRequestContent.multipartRequest;
import static com.stehno.ersatz.match.MultipartRequestMatcher.multipartMatcher;
import static com.stehno.ersatz.server.UnderlyingServer.NOT_FOUND_BODY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ErsatzServerExtension.class)
class ErsatzRequestWithContentTest {

    private static final String BODY_CONTENT = "{\"label\":\"Body Content\", \"text\":\"This is some body content.\"}";
    private ErsatzRequestWithContent request;
    private HttpClient client;
    private ErsatzServer server;

    @BeforeEach void beforeEach() {
        request = new ErsatzRequestWithContent(POST, equalTo("/posting"));
        client = new HttpClient();
    }

    @Test @DisplayName("boty with content-type")
    void bodyWithContentType() {
        request.body(BODY_CONTENT, TEXT_PLAIN).decoder(TEXT_PLAIN, Decoders.utf8String);

        final var clientRequest = new MockClientRequest(POST);
        clientRequest.setPath("/posting");
        clientRequest.setBody(BODY_CONTENT.getBytes());

        assertTrue(
            request.matches(clientRequest.header("Content-Type", TEXT_PLAIN.getValue()))
        );
    }

    @Test @DisplayName("to string")
    void string() {
        request.body("Some body", TEXT_PLAIN);

        assertEquals("Expectations (ErsatzRequestWithContent): <POST>, \"/posting\", a collection containing a string starting with \"text/plain\", \"Some body\", ", request.toString());
    }

    @Test @DisplayName("matching body")
    void matchingBody() throws IOException {
        server.expectations(e -> {
            e.POST("/posting", req -> {
                req.body(BODY_CONTENT, TEXT_PLAIN);
                req.decoder(TEXT_PLAIN, Decoders.utf8String);
                req.responder(res -> res.body("accepted", TEXT_PLAIN));
            });
        });

        var response = client.post(server.httpUrl("/posting"), RequestBody.create(BODY_CONTENT, MediaType.get("text/plain")));
        assertEquals("accepted", response.body().string());

        response = client.post(server.httpUrl("/posting"), RequestBody.create("", MediaType.get("text/plain")));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching: body and content-type")
    void matchingBodyAndContentType() throws IOException {
        server.expectations(e -> {
            e.POST("/posting", req -> {
                req.body(BODY_CONTENT, "text/plain; charset=utf-8");
                req.decoder(TEXT_PLAIN, Decoders.utf8String);
                req.responder(res -> res.body("accepted"));
            });
        });

        var response = client.post(server.httpUrl("/posting"), RequestBody.create(BODY_CONTENT, MediaType.get("text/plain; charset=utf-8")));
        assertEquals("accepted", response.body().string());

        response = client.post(server.httpUrl("/posting"), RequestBody.create(BODY_CONTENT, MediaType.get("text/html")));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    // TODO: replace
/*    @Test @DisplayName("matching body with converter (builder)")
    void matchingBodyWithConverter() throws IOException {
        server.expectations(e -> {
            e.POST("/posting", req -> {
                req.body(
                    Map.of(
                        "label", "Body Content",
                        "text", "This is some body content."
                    ),
                    "some/json; charset=utf-8"
                );
                req.decoder(new ContentType("some/json; charset=utf-8"), parseJson);
                req.responds().body("accepted");
            });
        });

        var response = client.post(server.httpUrl("/posting"), RequestBody.create(BODY_CONTENT, MediaType.get("some/json; charset=utf-8")));
        assertEquals("accepted", response.body().string());

        response = client.post(server.httpUrl("/posting"), RequestBody.create(BODY_CONTENT, MediaType.get("text/html")));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }*/

//    private static final BiFunction<byte[], DecodingContext, Object> parseJson = (content, ctx) -> new JsonSlurper().parse(content != null ? content : "{}".getBytes(UTF_8));

    @Test @DisplayName("application/x-www-form-urlencoded")
    void applicationFormEncoded() throws IOException {
        server.expectations(e -> {
            e.POST("/form", req -> {
                req.decoder(APPLICATION_URLENCODED, Decoders.urlEncoded);
                req.body(
                    Map.of(
                        "alpha", "some data",
                        "bravo", "42",
                        "charlie", "last"
                    ),
                    "application/x-www-form-urlencoded; charset=utf-8"
                );
                req.responds().body("ok");
            });
        });

        var response = client.post(server.httpUrl("/form"), RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "alpha=some+data&bravo=42&charlie=last"));
        assertEquals("ok", response.body().string());
    }

    @Test @DisplayName("multipart/form-data")
    void multipartFormData() throws IOException {
        server.expectations(e -> {
            e.POST("/upload", req -> {
                req.decoder(TEXT_PLAIN, Decoders.utf8String);
                req.decoder(MULTIPART_MIXED, Decoders.multipart);
                req.decoder(IMAGE_PNG, Decoders.passthrough);
                req.body(multipartRequest(mrc -> {
                    mrc.part("something", TEXT_PLAIN, "interesting");
                    mrc.part("infoFile", "info.txt", "text/plain; charset=utf-8", "This is some interesting file content.");
                    mrc.part("dataFile", "data.bin", IMAGE_PNG, new byte[]{8, 6, 7, 5, 3, 0, 9});
                }), MULTIPART_MIXED);
                req.responds().body("ok");
            });
        });

        final var bodyBuilder = new MultipartBody.Builder()
            .addFormDataPart("something", "interesting")
            .addFormDataPart("infoFile", "info.txt", RequestBody.create(MediaType.parse("text/plain"), "This is some interesting file content."))
            .addFormDataPart("dataFile", "data.bin", RequestBody.create(MediaType.parse("image/png"), new byte[]{8, 6, 7, 5, 3, 0, 9}));

        final var response = client.post(Map.of("Content-Type", "multipart/form-data"), server.httpUrl("/upload"), bodyBuilder.build());
        assertEquals("ok", response.body().string());
    }

    @Disabled("FIXME: seems to be an issue with the multipart matcher")
    @Test @DisplayName("multipart/form-data using matcher object")
    void multipartUsingMatcher() throws IOException {
        server.expectations(e -> {
            e.POST("/upload", req -> {
                req.decoder(TEXT_PLAIN, Decoders.utf8String);
                req.decoder(MULTIPART_MIXED, Decoders.multipart);
                req.decoder(IMAGE_PNG, Decoders.passthrough);
                req.body(multipartMatcher(mrm -> {
                    mrm.part("something", "interesting");
                    mrm.part("infoFile", "info.txt", "text/plain", equalTo("This is some interesting file content."));
                    mrm.part("dataFile", "data.bin", IMAGE_PNG, notNullValue());
                }), MULTIPART_MIXED);
                req.responds().body("ok");
            });
        });

        final var bodyBuilder = new MultipartBody.Builder()
            .addFormDataPart("something", "interesting")
            .addFormDataPart("infoFile", "info.txt", RequestBody.create(MediaType.parse("text/plain"), "This is some interesting file content."))
            .addFormDataPart("dataFile", "data.bin", RequestBody.create(MediaType.parse("image/png"), new byte[]{8, 6, 7, 5, 3, 0, 9}));

        final var response = client.post(Map.of("Content-Type", "multipart/form-data"), server.httpUrl("/upload"), bodyBuilder.build());
        assertEquals("ok", response.body().string());
    }
}