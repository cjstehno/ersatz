/**
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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.HttpMethod;
import com.stehno.ersatz.Request;
import com.stehno.ersatz.RequestDecoders;
import com.stehno.ersatz.ResponseEncoders;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class GetExpectationsTest {

    // FIXME: do similar testing for each set of expectations.

    private RequestDecoders decoders;
    private ResponseEncoders encoders;
    private ExpectationsImpl expectations;

    @BeforeEach void beforeEach() {
        decoders = new RequestDecoders();
        encoders = new ResponseEncoders();
        expectations = new ExpectationsImpl(decoders, encoders);
    }

    @ParameterizedTest @DisplayName("GET(path)")
    @CsvSource({
        "/foo,/foo,true",
        "/foo,/bar,false"
    })
    void get_path(final String requestPath, final String expectedPath, final boolean present) {
        final var request = request(requestPath);

        final var requestExpectation = expectations.GET(expectedPath);
        assertThat(requestExpectation, notNullValue(Request.class));

        final var match = expectations.findMatch(request);
        assertThat(match.isPresent(), equalTo(present));
    }

    @ParameterizedTest @DisplayName("GET(matcher)")
    @CsvSource({
        "/prefix/a,true",
        "/a,false"
    })
    void get_matcher(final String requestPath, final boolean present) {
        final var request = request(requestPath);

        final var requestExpectation = expectations.GET(startsWith("/prefix"));
        assertThat(requestExpectation, notNullValue(Request.class));

        final var match = expectations.findMatch(request);
        assertThat(match.isPresent(), equalTo(present));
    }

    @ParameterizedTest @DisplayName("GET(path,consumer)")
    @CsvSource({
        "alpha,alpha,true",
        "alpha,bravo,false"
    })
    void get_path_consumer(final String label, final String expectedLabel, final boolean present) {
        final var request = request("/blah");
        request.query("label", label);

        final var requestExpectation = expectations.GET("/blah", req -> req.query("label", expectedLabel));
        assertThat(requestExpectation, notNullValue(Request.class));

        final var match = expectations.findMatch(request);
        assertThat(match.isPresent(), equalTo(present));
    }

    private static MockClientRequest request(final String path){
        final var request = new MockClientRequest(HttpMethod.GET);
        request.setPath(path);
        return request;
    }
}