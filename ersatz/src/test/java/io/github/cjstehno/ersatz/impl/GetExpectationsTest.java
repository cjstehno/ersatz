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
package io.github.cjstehno.ersatz.impl;

import static org.hamcrest.CoreMatchers.startsWith;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GetExpectationsTest extends ExpectationHarness {

    GetExpectationsTest() {
        super(HttpMethod.GET);
    }

    @ParameterizedTest @DisplayName("GET(path)")
    @CsvSource({
        "/foo,/foo,true",
        "/foo,/bar,false"
    })
    void get_path(final String requestPath, final String expectedPath, final boolean exists) {
        execAndAssert(
            mock -> mock.setPath(requestPath),
            expectations -> expectations.GET(expectedPath),
            exists
        );
    }

    @ParameterizedTest @DisplayName("GET(matcher)")
    @CsvSource({
        "/prefix/a,true",
        "/a,false"
    })
    void get_matcher(final String requestPath, final boolean present) {
        execAndAssert(
            mock -> mock.setPath(requestPath),
            expectations -> expectations.GET(startsWith("/prefix")),
            present
        );
    }

    @ParameterizedTest @DisplayName("GET(path,consumer)")
    @CsvSource({
        "alpha,alpha,true",
        "alpha,bravo,false"
    })
    void get_path_consumer(final String label, final String expectedLabel, final boolean present) {
        execAndAssert(
            mock -> {
                mock.setPath("/blah");
                mock.query("label", label);
            },
            expectations -> expectations.GET("/blah", req -> req.query("label", expectedLabel)),
            present
        );
    }
}