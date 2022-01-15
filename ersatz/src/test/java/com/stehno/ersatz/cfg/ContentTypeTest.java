/**
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
package com.stehno.ersatz.cfg;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.stehno.ersatz.cfg.ContentType.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentTypeTest {

    @Test void propertiesPredefined() {
        assertEquals("application/x-www-form-urlencoded", APPLICATION_URLENCODED.getValue());
        assertEquals("text/html; charset=utf-8", TEXT_HTML.withCharset("utf-8").getValue());
        assertEquals("text/html; charset=utf-8", TEXT_HTML.withCharset(UTF_8).getValue());
    }

    @Test void propertiesCreated() {
        assertEquals("foo/bar", new ContentType("foo/bar").getValue());
        assertEquals("foo/bar; charset=utf-16", new ContentType("foo/bar").withCharset("utf-16").getValue());
    }

    @Test void string() {
        assertEquals("text/javascript", TEXT_JAVASCRIPT.toString());
        assertEquals("text/javascript; charset=us-ascii", TEXT_JAVASCRIPT.withCharset("us-ascii").toString());
    }
}