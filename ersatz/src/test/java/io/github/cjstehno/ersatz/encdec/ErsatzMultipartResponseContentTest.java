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
package io.github.cjstehno.ersatz.encdec;


import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON;
import static io.github.cjstehno.ersatz.cfg.ContentType.IMAGE_JPG;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static io.github.cjstehno.ersatz.encdec.MultipartResponseContent.multipartResponse;
import static io.github.cjstehno.testthings.Resources.resourceToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErsatzMultipartResponseContentTest {

    private static final Function<Object, byte[]> BYTE_ENCODER = o -> ((String) o).getBytes();

    private static Stream<String> expectedResponse() {
        return resourceToString("/multipart-response.txt").lines();
    }

    @Test @DisplayName("multipart content")
    void multipartContent() {
        final var multipartContent = (ErsatzMultipartResponseContent) multipartResponse(mult -> {
            mult.boundary("abc123");

            mult.encoder("text/plain", String.class, BYTE_ENCODER);
            mult.encoder(APPLICATION_JSON, String.class, BYTE_ENCODER);
            mult.encoder("image/jpeg", InputStream.class, Encoders.binaryBase64);

            mult.field("foo", "bar");

            mult.part("alpha", "text/plain", "This is some text");
            mult.part("bravo", APPLICATION_JSON, "{\"answer\":42}");
            mult.part("charlie", "charlie.txt", TEXT_PLAIN, "This is a text file");
            mult.part("charlie-2", "charlie-2.txt", "text/plain", "This is another text file");
            mult.part("delta", "delta.jpg", IMAGE_JPG, new ByteArrayInputStream("fake image content for testing".getBytes()), "base64");
        });

        assertEquals("multipart/mixed; boundary=abc123", multipartContent.getContentType());

        val expectedLines = expectedResponse();
        val response = new String(Encoders.multipart.apply(multipartContent)).lines();

        assertLinesMatch(expectedLines, response);
    }

    @Test @DisplayName("Encoder not found")
    void encoderNotFound() {
        val mrc = new ErsatzMultipartResponseContent();
        val thrown = assertThrows(IllegalArgumentException.class, () -> {
            mrc.encoder("foo/bar", String.class);
        });
        assertEquals("No encoder found for content-type (foo/bar) and object type (String).", thrown.getMessage());
    }
}
