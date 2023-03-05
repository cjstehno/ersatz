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
package io.github.cjstehno.ersatz.impl.matchers;

import io.github.cjstehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestSchemeMatcherTest {

    @ParameterizedTest @CsvSource({
        "true,HTTPS,true",
        "true,https,true",
        "true,http,false",
        "true,HTTP,false",
        "false,HTTPS,false",
        "false,https,false",
        "false,http,true",
        "false,HTTP,true"
    })
    void secureMatcher(final boolean secure, final String scheme, final boolean expects) {
        assertEquals(
            expects,
            new RequestSchemeMatcher(secure).matches(new MockClientRequest(GET, "/testing").scheme(scheme))
        );
    }
}