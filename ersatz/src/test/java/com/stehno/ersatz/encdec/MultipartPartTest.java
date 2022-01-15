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

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipartPartTest {

    @Test @DisplayName("properties") void properties() {
        final var part = new MultipartPart("file", "file.txt", "text/plain", "binary", "some file");

        assertEquals("file", part.getFieldName());
        assertEquals("file.txt", part.getFileName());
        assertEquals("text/plain", part.getContentType());
        assertEquals("binary", part.getTransferEncoding());
        assertEquals("some file", part.getValue());
    }
}
