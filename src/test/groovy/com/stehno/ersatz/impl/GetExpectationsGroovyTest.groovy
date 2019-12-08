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
