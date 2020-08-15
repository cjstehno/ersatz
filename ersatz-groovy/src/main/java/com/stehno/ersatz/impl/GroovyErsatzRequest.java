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

import com.stehno.ersatz.cfg.GroovyRequest;
import com.stehno.ersatz.cfg.GroovyResponse;
import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.cfg.Response;
import com.stehno.ersatz.encdec.ResponseEncoders;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.hamcrest.Matcher;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

public class GroovyErsatzRequest extends ErsatzRequest implements GroovyRequest {

    public GroovyErsatzRequest(final HttpMethod meth, final Matcher<String> pathMatcher, final ResponseEncoders globalEncoders, final boolean noResponse) {
        super(meth, pathMatcher, globalEncoders, noResponse);
    }

    public GroovyErsatzRequest(final HttpMethod meth, final Matcher<String> pathMatcher, final ResponseEncoders globalEncoders) {
        this(meth, pathMatcher, globalEncoders, false);
    }

    @Override
    public GroovyRequest responder(@DelegatesTo(value = GroovyResponse.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return (GroovyRequest) responder(ConsumerWithDelegate.create(closure));
    }

    @Override protected Response newResponse() {
        return new GroovyErsatzResponse(isEmptyResponse(), getGlobalEncoders());
    }
}
