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

import static com.stehno.ersatz.cfg.ContentType.IMAGE_PNG;
import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static com.stehno.ersatz.encdec.MultipartRequestContent.multipartRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipartRequestContentTest {

    @Test @DisplayName("consumer configuration") void consumer() {
        MultipartRequestContent content = multipartRequest(mrc -> {
            mrc.part("alpha", "one");
            mrc.part("bravo", "text/markdown", "this _is_ *rich^ text");
            mrc.part("charlie", IMAGE_PNG, new byte[]{8, 6, 7, 5, 3, 0, 9});
            mrc.part("delta", "delta.txt", "text/markdown", "this _is_ more text");
            mrc.part("echo", "some.png", IMAGE_PNG, new byte[]{4, 2});
        });

        assertEquals(new MultipartPart("alpha", null, TEXT_PLAIN.getValue(), null, "one"), content.getAt("alpha"));
        assertEquals(new MultipartPart("bravo", null, "text/markdown", null, "this _is_ *rich^ text"), content.getAt("bravo"));
        assertEquals(new MultipartPart("charlie", null, IMAGE_PNG.getValue(), null, new byte[]{8, 6, 7, 5, 3, 0, 9}), content.getAt("charlie"));
        assertEquals(new MultipartPart("delta", "delta.txt", "text/markdown", null, "this _is_ more text"), content.getAt("delta"));
        assertEquals(new MultipartPart("echo", "some.png", IMAGE_PNG.getValue(), null, new byte[]{4, 2}), content.getAt("echo"));
    }
}
