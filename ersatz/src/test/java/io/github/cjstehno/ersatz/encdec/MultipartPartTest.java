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
package io.github.cjstehno.ersatz.encdec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.cjstehno.ersatz.TestAssertions.verifyEqualityAndHashCode;
import static io.github.cjstehno.ersatz.cfg.ContentType.IMAGE_PNG;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipartPartTest {

    private MultipartPart multipartPart() {
        return new MultipartPart("file", "file.txt", "text/plain", "binary", "some file");
    }

    @Test @DisplayName("properties") void properties() {
        final var part = multipartPart();
        assertEquals("file", part.getFieldName());
        assertEquals("file.txt", part.getFileName());
        assertEquals("text/plain", part.getContentType());
        assertEquals("binary", part.getTransferEncoding());
        assertEquals("some file", part.getValue());
    }

    @Test @DisplayName("equals and hashCode")
    void equalsAndHash() {
        verifyEqualityAndHashCode(multipartPart(), multipartPart());
    }

    @Test @DisplayName("string")
    void string() {
        assertEquals(
            "MultipartPart(fieldName=file, fileName=file.txt, contentType=text/plain, transferEncoding=binary, value=some file)",
            multipartPart().toString()
        );
    }

    @Test @DisplayName("equalsAndHash with bytes")
    void equalsAndHashWithBytes(){
        verifyEqualityAndHashCode(
            new MultipartPart("charlie", null, IMAGE_PNG.getValue(), null, new byte[]{8, 6, 7, 5, 3, 0, 9}),
            new MultipartPart("charlie", null, IMAGE_PNG.getValue(), null, new byte[]{8, 6, 7, 5, 3, 0, 9})
        );
    }
}
