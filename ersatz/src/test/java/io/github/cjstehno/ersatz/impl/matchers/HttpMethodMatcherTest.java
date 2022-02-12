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
package io.github.cjstehno.ersatz.impl.matchers;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpMethodMatcherTest {

    @ParameterizedTest @CsvSource({
        "ANY,GET,true",
        "ANY,HEAD,true",
        "ANY,POST,true",
        "ANY,PUT,true",
        "ANY,PATCH,true",
        "ANY,OPTIONS,true",
        "ANY,DELETE,true",
        "ANY,TRACE,true",
        "GET,GET,true",
        "GET,PUT,false",
        "POST,POST,true",
        "PATCH,OPTIONS,false",
    })
    void methodMatching(final HttpMethod required, final HttpMethod requested, final boolean matched) {
        assertEquals(matched, new HttpMethodMatcher(required).matches(new MockClientRequest(requested, "/testing")));
    }
}