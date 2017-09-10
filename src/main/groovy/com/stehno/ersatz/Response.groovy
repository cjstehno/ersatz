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

import java.util.concurrent.TimeUnit
import java.util.function.Function

/**
 * Used to configure the provided response to an HTTP request.
 */
@CompileStatic
interface Response {

    /**
     * Defines the request content to be sent back to the client. In the case of <code>MultipartContent</code>, the content-type will also be set.
     *
     * @param content the content object
     * @return this response
     */
    Response content(final Object content)

    /**
     * Defines the request content to be sent back to the client, along with its content-type. Multipart responses may be specified using this method;
     * however, the content-type will need to specify the boundary string and the boundary will need to be specified in the
     * <code>MultipartContent</code> configuration as well.
     *
     * @param content the content object
     * @param contentType the content type
     * @return this response
     */
    Response content(final Object content, final String contentType)

    /**
     * Defines the request content to be sent back to the client, along with its content-type. Multipart responses may be specified using this method;
     * however, the content-type will need to specify the boundary string and the boundary will need to be specified in the
     * <code>MultipartContent</code> configuration as well.
     *
     * @param content the content object
     * @param contentType the content type
     * @return this response
     */
    Response content(final Object content, final ContentType contentType)

    /**
     * Used to add a header to the response with the given name and value.
     *
     * @param name the header name
     * @param value the header value
     * @return this response
     */
    Response header(final String name, final String... value)

    /**
     * Used to add multiple headers to the response.
     *
     * @param headers the headers
     * @return this response
     */
    Response headers(final Map<String, Object> headers)

    /**
     * Shortcut method for adding values to the "Allow" header. These values will be appended to the existing header.
     *
     * @param methods the allowed HTTP methods
     * @return this response
     */
    Response allows(final HttpMethod... methods)

    /**
     * Used to add a cookie to the response with the given name and value.
     *
     * @param name the cookie name
     * @param value the cookie value
     * @return this response
     */
    Response cookie(final String name, final String value)

    /**
     * Used to add a cookie to the response.
     *
     * @param name the cookie name
     * @param cookie the cookie definition
     * @return this response
     */
    Response cookie(final String name, final Cookie cookie)

    /**
     * Used to add multiple cookies to the response, with the provided names and values.
     *
     * @param cookies the cookies to be added
     * @return this response
     */
    Response cookies(final Map<String, String> cookies)

    /**
     * Used to specify the content type of the response.
     *
     * @param contentType the response content type
     * @return this response
     */
    Response contentType(final String contentType)

    /**
     * Used to specify the content type of the response.
     *
     * @param contentType the response content type
     * @return this response
     */
    Response contentType(final ContentType contentType)

    /**
     * Used to retrieve the content type of the response.
     *
     * @return the content type
     */
    String getContentType()

    /**
     * Used to specify the response code for the response.
     *
     * @param code the response code
     * @return this response
     */
    Response code(int code)

    /**
     * Used to specify a delay in the response time for the request. The response will not be returned to the client until the delay has passed.
     *
     * @param time the response delay in milliseconds
     * @return this response
     */
    Response delay(final long time)

    /**
     * Used to specify a delay in the response time for the request. The response will not be returned to the client until the delay has passed.
     *
     * @param time the response delay in the specified unit
     * @param unit the time unit to use
     * @return this response
     */
    Response delay(final long time, TimeUnit unit)

    /**
     * Used to specify a delay in the response time for the request as a <code>com.stehno.vanilla.util.TimeSpan</code> string (e.g. '2 s').
     *
     * @param time the delay time as a string
     * @return this response
     */
    Response delay(final String time)

    /**
     * Used to retrieve the response delay time.
     *
     * @return the response delay time in milliseconds
     */
    long getDelay()

    /**
     * Used to retrieve the configured response headers.
     *
     * @return the response headers
     */
    Map<String, List<String>> getHeaders()

    /**
     * Used to retrieve the configured response cookies. The map keys will be the cookie names, while the values will be either the cookie value as
     * as String, or a <code>Cookie</code> object.
     *
     * @return the response cookies
     */
    Map<String, Object> getCookies()

    /**
     * Used to retrieve the configured response content. The content will be converted to a String based on the encoder configured for the
     * content-type and content object type; if not encoder is found, the <code>toString()</code> method will be called on the content object.
     *
     * If no content exists, an empty string will be returned.
     *
     * @return the response content (encoded)
     */
    String getContent()

    /**
     * Used to retrieve the configured response code.
     *
     * @return the response code
     */
    Integer getCode()

    /**
     * Registers a response body encoder for this response, which will override any matching encoders configured globally (or shared).
     *
     * param contentType the response content-type to be encoded
     * @param objectType the response object type to be encoded
     * @param encoder the encoder function
     * @return a reference to this response configuration
     */
    Response encoder(String contentType, Class objectType, Function<Object, String> encoder)

    /**
     * Registers a response body encoder for this response, which will override any matching encoders configured globally (or shared).
     *
     * param contentType the response content-type to be encoded
     * @param objectType the response object type to be encoded
     * @param encoder the encoder function
     * @return a reference to this response configuration
     */
    Response encoder(ContentType contentType, Class objectType, Function<Object, String> encoder)

    /**
     * Registers the collection of shared encoders. Encoders registered on the response itself, will override these, but the shared encoders will
     * override any matching global encoders.
     *
     * @param encoders the shared encoders to be applied
     * @return a reference to this response configuration
     */
    Response encoders(ResponseEncoders encoders)
}
