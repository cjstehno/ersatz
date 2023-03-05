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

import io.github.cjstehno.ersatz.encdec.MimeTypes;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MimeTypesTest {

    @Test @DisplayName("valid mime type")
    void validMimeType() {
        val mt = MimeTypes.createMimeType("text/plain");
        assertEquals("text", mt.getPrimaryType());
        assertEquals("plain", mt.getSubType());
    }

    @Test @DisplayName("invalid mime type")
    void invalidMimeType() {
        val thrown = assertThrows(IllegalArgumentException.class, () -> {
            MimeTypes.createMimeType("blah");
        });
        assertEquals("Unable to find a sub type.", thrown.getMessage());
    }
}