/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package com.stehno.ersatz.issues;

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.cfg.ContentType;
import com.stehno.ersatz.encdec.Decoders;
import com.stehno.ersatz.encdec.DecodingContext;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.util.HttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.sql.XAConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.BiFunction;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static com.stehno.ersatz.cfg.ContentType.TEXT_XML;
import static com.stehno.ersatz.encdec.Decoders.utf8String;
import static com.stehno.ersatz.encdec.Encoders.text;
import static okhttp3.RequestBody.create;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ErsatzServerExtension.class)
class BodyContentMatcherTest {

    private static final String REQUEST_XML = "<request><node foo=\"bar\"/></request>";
    private static final String RESPONSE_XML = "<response>OK</response>";
    public static final String XML_CHARSET_UTF_8 = "text/xml; charset=utf-8";
    private ErsatzServer server;
    private HttpClient http;

    @BeforeEach void beforeEach() {
        http = new HttpClient();
    }

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

        final var response = http.post(server.httpUrl("/posting"), create(MediaType.get(XML_CHARSET_UTF_8), REQUEST_XML));

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

        var response = http.post(server.httpUrl("/posting"), create(MediaType.get("text/plain"), "Show me something good!"));

        assertEquals("you found it!", response.body().string());

        response = http.post(server.httpUrl("/posting"), create(MediaType.get("text/plain"), "You have nothing for me!"));

        assertEquals(404, response.code());

        assertTrue(server.verify());
    }

    // TODO: this might be useful to keep around
    private static class DebuggingMatcher<T> extends BaseMatcher<T> {

        private static final Logger log = LoggerFactory.getLogger(DebuggingMatcher.class);
        private final Matcher<T> matcher;

        private DebuggingMatcher(final Matcher<T> matcher) {
            this.matcher = matcher;
        }

        public static <R> DebuggingMatcher<R> debugging(final Matcher<R> matcher) {
            return new DebuggingMatcher<>(matcher);
        }

        @Override public boolean matches(final Object actual) {
            log.debug("Attempting to match ({}) with matcher: {}", actual, matcher);
            final var matches = matcher.matches(actual);

            log.debug("Matcher ({}) resulted in: {}", matcher, matches);

            return matches;
        }

        @Override public void describeTo(Description description) {
            // nothing
        }
    }
}
