/*
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
package io.github.cjstehno.ersatz.match

import io.github.cjstehno.ersatz.encdec.Cookie
import io.github.cjstehno.ersatz.encdec.MultipartRequestContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON
import static io.github.cjstehno.ersatz.match.MultipartRequestMatcher.multipartMatcher
import static org.hamcrest.Matchers.equalTo
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class MatchExtensionsTest {

    @ParameterizedTest
    @CsvSource([
        '/foo,stuff,true,true',
        '/foo,other,true,false',
        '/bar,stuff,true,false',
    ])
    void cookieMatcherExtension(final String cookiePath, final String cookieValue, final boolean http, final boolean result) {
        def matcher = CookieMatcher.cookieMatcher {
            path '/foo'
            value 'stuff'
            httpOnly true
        }

        assertEquals(
            result,
            matcher.matches(Cookie.cookie {
                path cookiePath
                value cookieValue
                httpOnly http
            })
        )
    }

    @Test void multipartRequestMatcherExtension() {
        def content = MultipartRequestContent.multipartRequest {
            part 'alpha', 'one'
            part 'bravo', 'bravo.dat', APPLICATION_JSON, '{"label":"This is content!"}'
        }

        assertTrue(
            multipartMatcher {
                part 'alpha', equalTo('one')
            }.matches(content)
        )

        assertFalse(
            multipartMatcher {
                part 'alpha', equalTo('nope')
            }.matches(content)
        )
    }
}