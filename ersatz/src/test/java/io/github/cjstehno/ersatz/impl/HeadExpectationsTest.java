/**
 * Copyright (C) 2024 Christopher J. Stehno
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

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.startsWith;

public class HeadExpectationsTest extends ExpectationHarness {

    HeadExpectationsTest() {
        super(HttpMethod.HEAD);
    }

    @ParameterizedTest @DisplayName("HEAD(path)")
    @CsvSource({
        "/foo,/foo,true",
        "/foo,/bar,false"
    })
    void headPath(final String requestPath, final String expectedPath, final boolean exists) {
        execAndAssert(
            mock -> mock.setPath(requestPath),
            expectations -> expectations.HEAD(expectedPath),
            exists
        );
    }

    @ParameterizedTest @DisplayName("HEAD(matcher)")
    @CsvSource({
        "/prefix/a,true",
        "/a,false"
    })
    void headMatcher(final String requestPath, final boolean present) {
        execAndAssert(
            mock -> mock.setPath(requestPath),
            expectations -> expectations.HEAD(startsWith("/prefix")),
            present
        );
    }

    @ParameterizedTest @DisplayName("HEAD(path,consumer)")
    @CsvSource({
        "alpha,alpha,true",
        "alpha,bravo,false"
    })
    void headPathConsumer(final String label, final String expectedLabel, final boolean present) {
        execAndAssert(
            mock -> {
                mock.setPath("/blah");
                mock.query("label", label);
            },
            expectations -> expectations.HEAD("/blah", req -> req.query("label", expectedLabel)),
            present
        );
    }
}