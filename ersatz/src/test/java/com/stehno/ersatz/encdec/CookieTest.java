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
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieTest {

    @Test @DisplayName("cookie (closure)") void cookieClosure() {
        Cookie cookie = Cookie.cookie(c -> {
            c.value("alpha");
            c.comment("Something");
            c.domain("localhost");
            c.path("/foo");
            c.version(1);
            c.httpOnly(true);
            c.maxAge(100);
            c.secure(true);
        });

        assertEquals("alpha", cookie.getValue());
        assertEquals("Something", cookie.getComment());
        assertEquals("localhost", cookie.getDomain());
        assertEquals("/foo", cookie.getPath());
        assertEquals(1, cookie.getVersion());
        assertTrue(cookie.isHttpOnly());
        assertEquals(100, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
    }
}
