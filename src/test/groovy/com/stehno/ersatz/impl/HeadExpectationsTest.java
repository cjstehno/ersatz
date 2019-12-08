package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.HttpMethod;
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
    void head_path(final String requestPath, final String expectedPath, final boolean exists) {
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
    void head_matcher(final String requestPath, final boolean present) {
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
    void head_path_consumer(final String label, final String expectedLabel, final boolean present) {
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