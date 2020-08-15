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

import com.stehno.ersatz.cfg.ChunkingConfig;
import com.stehno.ersatz.cfg.GroovyResponse;
import com.stehno.ersatz.encdec.ResponseEncoders;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

public class GroovyErsatzResponse extends ErsatzResponse implements GroovyResponse {

    public GroovyErsatzResponse(final boolean empty, final ResponseEncoders globalEncoders) {
        super(empty, globalEncoders);
    }

    @Override
    public GroovyResponse chunked(@DelegatesTo(value = ChunkingConfig.class, strategy = DELEGATE_FIRST) Closure closure) {
        return (GroovyResponse) chunked(ConsumerWithDelegate.create(closure));
    }
}
