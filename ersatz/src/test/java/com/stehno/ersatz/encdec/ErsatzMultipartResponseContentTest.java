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
package com.stehno.ersatz.encdec;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

import static com.stehno.ersatz.cfg.ContentType.*;
import static com.stehno.ersatz.encdec.MultipartResponseContent.multipartResponse;
import static org.apache.commons.io.IOUtils.readLines;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ErsatzMultipartResponseContentTest {

    private static final Function<Object, byte[]> BYTE_ENCODER = o -> ((String) o).getBytes();

    private static List<String> expectedResponse() throws IOException {
        return readLines(ErsatzMultipartResponseContent.class.getResourceAsStream("/multipart-response.txt"));
    }

    @Test @DisplayName("multipart content")
    void multipartContent() throws Exception {
        final var multipartContent = (ErsatzMultipartResponseContent) multipartResponse(mult -> {
            mult.boundary("abc123");

            mult.encoder("text/plain", String.class, BYTE_ENCODER);
            mult.encoder(APPLICATION_JSON, String.class, BYTE_ENCODER);
            mult.encoder("image/jpeg", InputStream.class, Encoders.binaryBase64);

            mult.field("foo", "bar");

            mult.part("alpha", "text/plain", "This is some text");
            mult.part("bravo", APPLICATION_JSON, "{\"answer\":42}");
            mult.part("charlie", "charlie.txt", TEXT_PLAIN, "This is a text file");
            mult.part("delta", "delta.jpg", IMAGE_JPG, new ByteArrayInputStream("fake image content for testing".getBytes()), "base64");
        });

        assertEquals("multipart/mixed; boundary=abc123", multipartContent.getContentType());

        final var expectedLines = expectedResponse();
        final var response = new String(Encoders.multipart.apply(multipartContent)).split("\n");

        assertEquals(expectedLines.size(), response.length);

        for (int li = 0; li < expectedLines.size(); li++) {
            assertEquals(expectedLines.get(li).trim(), response[li].trim());
        }
    }
}
