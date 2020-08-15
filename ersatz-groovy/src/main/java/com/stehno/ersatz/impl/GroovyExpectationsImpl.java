/**
 * Copyright (C) 2020 Christopher J. Stehno
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

import com.stehno.ersatz.cfg.GroovyExpectations;
import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.cfg.Request;
import com.stehno.ersatz.cfg.RequestWithContent;
import org.hamcrest.Matcher;

import java.util.function.Consumer;

public class GroovyExpectationsImpl extends ExpectationsImpl implements GroovyExpectations {

    @Override
    public Request ANY(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return applyExpectation(new GroovyErsatzRequestWithContent(HttpMethod.ANY, matcher, getGlobalDecoders(), getGlobalEncoders()), consumer);
    }

    @Override
    public Request GET(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return applyExpectation(new GroovyErsatzRequest(HttpMethod.GET, matcher, getGlobalEncoders()), consumer);
    }

    @Override
    public Request HEAD(final Matcher<String> matcher, final Consumer<Request> consumer) {
        return applyExpectation(new GroovyErsatzRequest(HttpMethod.HEAD, matcher, getGlobalEncoders(), true), consumer);
    }

    @Override
    public RequestWithContent POST(Matcher<String> matcher, Consumer<RequestWithContent> consumer) {
        return applyExpectation(new GroovyErsatzRequestWithContent(HttpMethod.POST, matcher, getGlobalDecoders(), getGlobalEncoders()), consumer);
    }

    @Override
    public RequestWithContent PUT(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        return applyExpectation(new GroovyErsatzRequestWithContent(HttpMethod.PUT, matcher, getGlobalDecoders(), getGlobalEncoders()), config);
    }

    @Override
    public Request DELETE(Matcher<String> matcher, Consumer<Request> config) {
        return applyExpectation(new GroovyErsatzRequest(HttpMethod.DELETE, matcher, getGlobalEncoders()), config);
    }

    @Override
    public RequestWithContent PATCH(Matcher<String> matcher, Consumer<RequestWithContent> config) {
        return applyExpectation(new GroovyErsatzRequestWithContent(HttpMethod.PATCH, matcher, getGlobalDecoders(), getGlobalEncoders()), config);
    }

    @Override
    public Request OPTIONS(Matcher<String> matcher, Consumer<Request> config) {
        return applyExpectation(new GroovyErsatzRequest(HttpMethod.OPTIONS, matcher, getGlobalEncoders()), config);
    }
}
