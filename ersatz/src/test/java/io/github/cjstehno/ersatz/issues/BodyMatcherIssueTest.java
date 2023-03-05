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
package io.github.cjstehno.ersatz.issues;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import okhttp3.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.github.cjstehno.ersatz.TestAssertions.verify;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_XML;
import static io.github.cjstehno.ersatz.encdec.Decoders.utf8String;
import static io.github.cjstehno.ersatz.encdec.Encoders.text;
import static okhttp3.RequestBody.create;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
class BodyMatcherIssueTest {

    private static final String REQUEST_XML = "<request><node foo=\"bar\"/></request>";
    private static final String RESPONSE_XML = "<response>OK</response>";
    public static final String XML_CHARSET_UTF_8 = "text/xml; charset=utf-8";
    private ErsatzServer server;
    private HttpClientExtension.Client client;

    @Test @DisplayName("matching all of body content")
    void matchingAllBodyContent() throws IOException {
        server.expectations(e -> {
            e.POST("/posting", req -> {
                req.decoder(XML_CHARSET_UTF_8, utf8String);
                req.body(REQUEST_XML, XML_CHARSET_UTF_8);
                req.responder(res -> {
                    res.encoder(TEXT_XML, String.class, text);
                    res.body(RESPONSE_XML, TEXT_XML);
                });
            });
        });

        final var response = client.post("/posting", create(REQUEST_XML, MediaType.get(XML_CHARSET_UTF_8)));
        assertEquals(RESPONSE_XML, response.body().string());
    }

    @Test @DisplayName("matching partial body content") void matchingPartialContent() throws IOException {
        server.expectations(e -> {
            e.POST("/posting", req -> {
                req.decoder(TEXT_PLAIN, utf8String);
                req.body(containsString("something good"), TEXT_PLAIN);
                req.called(1);
                req.responder(res -> {
                    res.body("you found it!", TEXT_PLAIN);
                });
            });
        });

        var response = client.post("/posting", create("Show me something good!", MediaType.get("text/plain")));
        assertEquals("you found it!", response.body().string());

        response = client.post("/posting", create("You have nothing for me!", MediaType.get("text/plain")));
        assertEquals(404, response.code());

        verify(server);
    }
}
