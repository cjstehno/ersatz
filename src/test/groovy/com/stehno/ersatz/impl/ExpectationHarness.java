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
