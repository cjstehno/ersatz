package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.startsWith;

class PatchExpectationsTest extends ExpectationHarness {

    PatchExpectationsTest() {
        super(HttpMethod.PATCH);
    }

    @ParameterizedTest @DisplayName("PATCH(path)")
    @CsvSource({
        "/foo,/foo,true",
        "/foo,/bar,false"
    })
    void patch_path(final String requestPath, final String expectedPath, final boolean exists) {
        execAndAssert(
            mock -> mock.setPath(requestPath),
            expectations -> expectations.PATCH(expectedPath),
            exists
        );
    }

    @ParameterizedTest @DisplayName("PATCH(matcher)")
    @CsvSource({
        "/prefix/a,true",
        "/a,false"
    })
    void patch_matcher(final String requestPath, final boolean present) {
        execAndAssert(
            mock -> mock.setPath(requestPath),
            expectations -> expectations.PATCH(startsWith("/prefix")),
            present
        );
    }

    @ParameterizedTest @DisplayName("PATCH(path,consumer)")
    @CsvSource({
        "alpha,alpha,true",
        "alpha,bravo,false"
    })
    void patch_path_consumer(final String label, final String expectedLabel, final boolean present) {
        execAndAssert(
            mock -> {
                mock.setPath("/blah");
                mock.query("label", label);
            },
            expectations -> expectations.PATCH("/blah", req -> req.query("label", expectedLabel)),
            present
        );
    }
}
