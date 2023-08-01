/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.ContentType;
import io.github.cjstehno.ersatz.cfg.Response;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import java.util.function.Function;

/**
 * A <code>Response</code> implementation with no body content.
 */
public class ErsatzResponseWithoutContent extends ErsatzResponse {

    /**
     * Creates an empty response.
     */
    public ErsatzResponseWithoutContent() {
        super(null);
    }

    @Override
    public Response body(Object content) {
        throw new IllegalArgumentException("The response is configured as EMPTY and cannot have content.");
    }

    @Override
    public Response encoder(final String contentType, final Class objectType, final Function<Object, byte[]> encoder) {
        throw new UnsupportedOperationException("Configuring encoders for an EMPTY response is not allowed.");
    }

    @Override
    public Response encoder(final ContentType contentType, final Class objectType, final Function<Object, byte[]> encoder) {
        throw new UnsupportedOperationException("Configuring encoders for an EMPTY response is not allowed.");
    }

    @Override
    public Response encoders(final ResponseEncoders encoders) {
        throw new UnsupportedOperationException("Configuring encoders for an EMPTY response is not allowed.");
    }
}
