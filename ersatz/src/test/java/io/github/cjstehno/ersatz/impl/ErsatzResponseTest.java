/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON;
import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_XML;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErsatzResponseTest {

    private static final String CONTENT_A = "content-A";
    private ErsatzResponse response;

    @BeforeEach void beforeEach() {
        response = new ErsatzResponse(new ResponseEncoders());
    }

    @Test @DisplayName("content when empty")
    void contentWhenEmpty() {
        final var thrown = assertThrows(IllegalArgumentException.class, () -> {
            new ErsatzResponseWithoutContent().body(CONTENT_A);
        });
        assertEquals("The response is configured as EMPTY and cannot have content.", thrown.getMessage());
    }

    @Test @DisplayName("content")
    void content() {
        response.body(CONTENT_A);

        assertArrayEquals(CONTENT_A.getBytes(), response.getContent());
    }

    @Test @DisplayName("content and content-type")
    void contentAndContentType() {
        response.body(CONTENT_A, "text/info");

        assertArrayEquals(CONTENT_A.getBytes(), response.getContent());
        assertEquals("text/info", response.getContentType());
    }

    @Test @DisplayName("content and content-type object")
    void contentAndContentTypeObject() {
        response.body(CONTENT_A, APPLICATION_JSON);

        assertArrayEquals(CONTENT_A.getBytes(), response.getContent());
        assertEquals(APPLICATION_JSON.getValue(), response.getContentType());
    }

    @Test @DisplayName("content-type")
    void contentType() {
        response.contentType("text/info");

        assertEquals("text/info", response.getContentType());
    }

    @Test @DisplayName("content-type object")
    void contentTypeObject() {
        response.contentType(APPLICATION_XML);

        assertEquals(APPLICATION_XML.getValue(), response.getContentType());
    }

    @Test @DisplayName("headers")
    void headers() {
        response.headers(
            Map.of(
                "alpha", "something",
                "bravo", "other",
                "charlie", List.of("one", "two")
            )
        );

        assertEquals("something", response.getHeaders().get("alpha").get(0));
        assertEquals("other", response.getHeaders().get("bravo").get(0));
        assertEquals(List.of("one", "two"), response.getHeaders().get("charlie"));
    }

    @Test @DisplayName("header")
    void header() {
        response.header("one", "two");

        assertEquals("two", response.getHeaders().get("one").get(0));
    }

    @Test @DisplayName("header (multiple)")
    void headerMultiple() {
        response.header("one", "two", "three");

        assertEquals(List.of("two", "three"), response.getHeaders().get("one"));
    }

    @Test @DisplayName("cookies")
    void cookies() {
        response.cookies(Map.of(
            "alpha", "something",
            "bravo", "other"
        ));

        assertEquals("something", response.getCookies().get("alpha"));
        assertEquals("other", response.getCookies().get("bravo"));
    }

    @Test @DisplayName("cookie")
    void cookie() {
        response.cookie("one", "two");

        assertEquals("two", response.getCookies().get("one"));
    }

    @Test @DisplayName("code")
    void code() {
        response.code(505);

        assertEquals(505, response.getCode());
    }

    @Test @DisplayName("register encoder (string)")
    void registerEncoderString() {
        response.body("foo");
        response.encoder("text/plain", String.class, o -> (o + "-bar").getBytes());

        assertArrayEquals("foo-bar".getBytes(), response.getContent());
    }

    @Test @DisplayName("register encoder (object)")
    void registerEncoderObject() {
        response.body("foo");
        response.encoder(TEXT_PLAIN, String.class, o -> (o + "-bar").getBytes());

        assertArrayEquals("foo-bar".getBytes(), response.getContent());
    }

    @Test @DisplayName("register encoders")
    void registerEncoders() {
        response.encoders(ResponseEncoders.encoders(e -> {
            e.register(TEXT_PLAIN, String.class, o -> (o + "-baz").getBytes());
        }));

        response.body("foo");

        assertArrayEquals("foo-baz".getBytes(), response.getContent());
    }
}
