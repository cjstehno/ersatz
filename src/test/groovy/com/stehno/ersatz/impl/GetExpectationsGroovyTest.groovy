/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.impl

import com.stehno.ersatz.cfg.HttpMethod
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GetExpectationsGroovyTest extends ExpectationHarness {

    GetExpectationsGroovyTest() {
        super(HttpMethod.GET)
    }

    @ParameterizedTest @DisplayName("GET(path,closure)")
    @CsvSource([
            "alpha,alpha,true",
            "alpha,bravo,false"
    ])
    void get_path_closure(final String label, final String expectedLabel, final boolean present) {
        execAndAssert(
                { mock ->
                    mock.setPath("/blah");
                    mock.query("label", label);
                },
                { expectations ->
                    expectations.GET('/blah') {
                        query('label', expectedLabel)
                    }
                },
                present
        )
    }
}
