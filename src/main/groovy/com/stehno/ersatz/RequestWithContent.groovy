/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz

import groovy.transform.CompileStatic
import org.hamcrest.Matcher

import java.util.function.BiFunction

/**
 * Expectation configuration for a request with body content.
 */
@CompileStatic
interface RequestWithContent extends Request {

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body the body content
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Object body, String contentType)

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param matcher the body content matcher
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Matcher<Object> body, String contentType)

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body the body content
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Object body, ContentType contentType)

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param matcher the body content matcher
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Matcher<Object> body, ContentType contentType)

    /**
     * Specifies a custom body content converter function. The function will have the client request body content as a byte array and it will be
     * converted into the specified output type. Generally the conversion is used when comparing the client request with the configured request
     * body expectation.
     *
     * @param contentType the content type that the convert will handle
     * @param converter the conversion function
     * @return a reference to this request
     */
    RequestWithContent decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder)

    /**
     * Specifies a custom body content converter function. The function will have the client request body content as a byte array and it will be
     * converted into the specified output type. Generally the conversion is used when comparing the client request with the configured request
     * body expectation.
     *
     * @param contentType the content type that the convert will handle
     * @param converter the conversion function
     * @return a reference to this request
     */
    RequestWithContent decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder)

    /**
     * Configures a parent collection of decoders to be searched when a decoder is not configured on the request itself.
     *
     * @param decoders the parent decoder collection
     * @return a reference to this request
     */
    RequestWithContent decoders(final RequestDecoders decoders)
}
