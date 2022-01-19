/**
 * Copyright (C) 2022 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.encdec;


import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.stehno.ersatz.TestAssertions.verifyEqualityAndHashCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieTest {

    private static Cookie cookie() {
        return Cookie.cookie(c -> {
            c.value("alpha");
            c.comment("Something");
            c.domain("localhost");
            c.path("/foo");
            c.version(1);
            c.httpOnly(true);
            c.maxAge(100);
            c.secure(true);
        });
    }

    @Test @DisplayName("cookie (closure)") void cookieClosure() {
        val cookie = cookie();

        assertEquals("alpha", cookie.getValue());
        assertEquals("Something", cookie.getComment());
        assertEquals("localhost", cookie.getDomain());
        assertEquals("/foo", cookie.getPath());
        assertEquals(1, cookie.getVersion());
        assertTrue(cookie.isHttpOnly());
        assertEquals(100, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
    }

    @Test void equalsAndHash() {
        verifyEqualityAndHashCode(cookie(), cookie());
    }

    @Test void string() {
        assertEquals(
            "Cookie(value=alpha, comment=Something, domain=localhost, path=/foo, version=1, httpOnly=true, maxAge=100, secure=true)",
            cookie().toString()
        );
    }
}
