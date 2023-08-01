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
package io.github.cjstehno.ersatz.match;

import static io.github.cjstehno.ersatz.encdec.Cookie.cookie;
import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CookieMatcherTest {

    @ParameterizedTest @DisplayName("cookie matcher config")
    @CsvSource({
        "/foo,stuff,true,true",
        "/foo,other,true,false",
        "/bar,stuff,true,false"
    })
    void cookieMatcherConfig(final String cookiePath, final String cookieValue, final boolean http, final boolean result) {
        val matcher = CookieMatcher.cookieMatcher(c -> {
            c.path("/foo");
            c.value("stuff");
            c.httpOnly(true);
        });

        assertEquals(
            result,
            matcher.matches(cookie(c -> c.path(cookiePath).value(cookieValue).httpOnly(http)))
        );
    }
}