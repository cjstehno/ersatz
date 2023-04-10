/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;


import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.ContentType;
import io.github.cjstehno.ersatz.encdec.Decoders;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.JsonEncDec;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Map;

import static io.github.cjstehno.ersatz.cfg.ContentType.*;
import static io.github.cjstehno.ersatz.cfg.HttpMethod.POST;
import static io.github.cjstehno.ersatz.encdec.MultipartRequestContent.multipartRequest;
import static io.github.cjstehno.ersatz.match.MultipartRequestMatcher.multipartMatcher;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static io.github.cjstehno.ersatz.server.UnderlyingServer.NOT_FOUND_BODY;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
class ErsatzRequestWithContentTest {

    private static final String BODY_CONTENT = "{\"label\":\"Body Content\", \"text\":\"This is some body content.\"}";
    private ErsatzRequestWithContent request;
    private HttpClientExtension.Client client;
    private ErsatzServer server;

    @BeforeEach void beforeEach() {
        request = new ErsatzRequestWithContent(POST, pathMatching("/posting"), null, null);
    }

    @Test @DisplayName("body with content-type")
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

        assertEquals("Expectations (ErsatzRequestWithContent): HTTP method is (<POST>), Path matches \"/posting\", Body is \"Some body\" and content-type is a string starting with text/plain, and Called ANYTHING", request.toString());
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

        var response = client.post("/posting", create(BODY_CONTENT, MediaType.get("text/plain")));
        assertEquals("accepted", response.body().string());

        response = client.post("/posting", create("", MediaType.get("text/plain")));
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

        var response = client.post("/posting", create(BODY_CONTENT, MediaType.get("text/plain; charset=utf-8")));
        assertEquals("accepted", response.body().string());

        response = client.post("/posting", create(BODY_CONTENT, MediaType.get("text/html")));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

    @Test @DisplayName("matching body with converter (builder)")
    void matchingBodyWithConverter() throws IOException {
        val contentType = "some/json; charset=utf-8";
        server.expectations(e -> {
            e.POST("/posting", req -> {
                req.body(
                    Map.of(
                        "label", "Body Content",
                        "text", "This is some body content."
                    ),
                    contentType
                );
                req.decoder(new ContentType(contentType), JsonEncDec.jsonDecoder);
                req.responds().body("accepted");
            });
        });

        var response = client.post("/posting", create(BODY_CONTENT, MediaType.get(contentType)));
        assertEquals("accepted", response.body().string());

        response = client.post("/posting", create(BODY_CONTENT, MediaType.get("text/html")));
        assertEquals(NOT_FOUND_BODY, response.body().string());
    }

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

        var response = client.post("/form", create("alpha=some+data&bravo=42&charlie=last", parse("application/x-www-form-urlencoded")));
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

        val bodyBuilder = new MultipartBody.Builder()
            .addFormDataPart("something", "interesting")
            .addFormDataPart("infoFile", "info.txt", create("This is some interesting file content.", parse("text/plain")))
            .addFormDataPart("dataFile", "data.bin", create(new byte[]{8, 6, 7, 5, 3, 0, 9}, parse("image/png")));

        val response = client.post(
            "/upload",
            builder -> builder.header("Content-Type", "multipart/form-data"),
            bodyBuilder.build()
        );
        assertEquals("ok", response.body().string());
    }

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
            .addFormDataPart("infoFile", "info.txt", create(parse("text/plain"), "This is some interesting file content."))
            .addFormDataPart("dataFile", "data.bin", create(parse("image/png"), new byte[]{8, 6, 7, 5, 3, 0, 9}));

        val response = client.post(
            "/upload",
            builder -> builder.header("Content-Type", "multipart/form-data"),
            bodyBuilder.build()
        );
        assertEquals("ok", response.body().string());
    }
}