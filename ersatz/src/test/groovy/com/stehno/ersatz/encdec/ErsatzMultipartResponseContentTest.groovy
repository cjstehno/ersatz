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
package com.stehno.ersatz.encdec


import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import java.util.function.Consumer
import java.util.function.Function

import static com.stehno.ersatz.cfg.ContentType.*
import static org.junit.jupiter.api.Assertions.assertEquals

class ErsatzMultipartResponseContentTest {

    private static List<String> expectedResponse() throws IOException {
        ErsatzMultipartResponseContent.getResourceAsStream("/multipart-response.txt").readLines()
    }

    private static final Function<Object, byte[]> BYTE_ENCODER = new Function<Object, byte[]>() {
        @Override byte[] apply(Object o) {
            ((String) o).bytes
        }
    }

    @Test @DisplayName("multipart content: consumer")
    void multipartConsumer() throws Exception {
        def multipartContent = (ErsatzMultipartResponseContent) MultipartResponseContent.multipartResponse(new Consumer<MultipartResponseContent>() {
            @Override void accept(final MultipartResponseContent mult) {
                mult.boundary("abc123");

                mult.encoder("text/plain", String.class, BYTE_ENCODER);
                mult.encoder(APPLICATION_JSON, String.class, BYTE_ENCODER);
                mult.encoder("image/jpeg", InputStream.class, Encoders.binaryBase64);

                mult.field("foo", "bar");

                mult.part("alpha", "text/plain", "This is some text");
                mult.part("bravo", APPLICATION_JSON, "{\"answer\":42}");
                mult.part("charlie", "charlie.txt", TEXT_PLAIN, "This is a text file");
                mult.part("delta", "delta.jpg", IMAGE_JPG, new ByteArrayInputStream("fake image content for testing".getBytes()), "base64");
            }
        });

        assertEquals("multipart/mixed; boundary=abc123", multipartContent.getContentType());

        def expectedLines = expectedResponse();
        def response = new String(Encoders.multipart.apply(multipartContent)).split("\n");

        assertEquals(expectedLines.size(), response.length);

        for (int li = 0; li < expectedLines.size(); li++) {
            assertEquals(
                expectedLines.get(li).trim(),
                response[li].trim()
            );
        }
    }

    @Test @DisplayName('multipart content: closure')
    void multipartClosure() throws Exception {
        MultipartResponseContent mc = MultipartResponseContent.multipartResponse {
            boundary 'abc123'

            encoder 'text/plain', String, { o -> (o as String).bytes }
            encoder APPLICATION_JSON, String, { o -> (o as String).bytes }
            encoder 'image/jpeg', InputStream, Encoders.binaryBase64

            field 'foo', 'bar'

            part 'alpha', 'text/plain', 'This is some text'
            part 'bravo', APPLICATION_JSON, '{"answer":42}'
            part 'charlie', 'charlie.txt', TEXT_PLAIN, 'This is a text file'
            part 'delta', 'delta.jpg', IMAGE_JPG, new ByteArrayInputStream('fake image content for testing'.bytes), 'base64'
        }

        assertEquals('multipart/mixed; boundary=abc123', mc.contentType)

        def expectedLines = expectedResponse();
        def response = new String(Encoders.multipart.apply(mc)).split('\n');

        assertEquals(expectedLines.size(), response.length);

        for (int li = 0; li < expectedLines.size(); li++) {
            assertEquals(
                expectedLines.get(li).trim(),
                response[li].trim()
            );
        }
    }
}
