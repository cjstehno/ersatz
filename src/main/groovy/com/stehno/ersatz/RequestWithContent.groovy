/*
 * Copyright (C) 2016 Christopher J. Stehno
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

import java.util.function.Function

/**
 * An expectation of a request with body content.
 */
@CompileStatic
interface RequestWithContent extends Request {

    /**
     * Configures the expected body content of the request.
     *
     * @param body the body content
     * @return a reference to this request
     */
    RequestWithContent body(final Object body)

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body the body content
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Object body, String contentType)
    RequestWithContent body(final Object body, ContentType contentType)

    /**
     * Configures the expected content type (header) of the request.
     *
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent contentType(final String contentType)
    RequestWithContent contentType(final ContentType contentType)

    /**
     * Specifies a custom body content converter function. The function will have the client request body content as a byte array and it will be
     * converted into the specified output type. Generally the conversion is used when comparing the client request with the configured request
     * body expectation.
     *
     * @param contentType the content type that the convert will handle
     * @param converter the conversion function
     * @return a reference to this request
     */
    RequestWithContent converter(final String contentType, final Function<byte[], Object> converter)
    RequestWithContent converter(final ContentType contentType, final Function<byte[], Object> converter)
}
