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

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.cfg.Request;
import com.stehno.ersatz.encdec.RequestDecoders;
import com.stehno.ersatz.encdec.ResponseEncoders;
import com.stehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.api.BeforeEach;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

abstract class ExpectationHarness {

    private final HttpMethod method;
    private ExpectationsImpl expectations;

    ExpectationHarness(final HttpMethod method) {
        this.method = method;
    }

    @BeforeEach public void beforeEach() {
        expectations = new ExpectationsImpl(new RequestDecoders(), new ResponseEncoders());
    }

    void execAndAssert(final Consumer<MockClientRequest> requestConfig, final Function<ExpectationsImpl, Request> methodExecutor, final boolean matchExists) {
        final var request = new MockClientRequest(method);
        requestConfig.accept(request);

        final var requestExpectation = methodExecutor.apply(expectations);
        assertThat(requestExpectation, notNullValue(Request.class));

        final var match = expectations.findMatch(request);
        assertThat(match.isPresent(), equalTo(matchExists));
    }
}
