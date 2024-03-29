/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.cfg;

import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.cjstehno.ersatz.util.HttpHeaders.ALLOW;
import static io.github.cjstehno.ersatz.util.HttpHeaders.CONTENT_TYPE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Used to configure the provided response to an HTTP request.
 */
public interface Response {

    /**
     * Defines the request content to be sent back to the client. In the case of <code>MultipartContent</code>, the content-type will also be set.
     *
     * @param content the content object
     * @return this response
     */
    Response body(Object content);

    /**
     * Defines the request content to be sent back to the client, along with its content-type. Multipart responses may be specified using this method;
     * however, the content-type will need to specify the boundary string and the boundary will need to be specified in the
     * <code>MultipartContent</code> configuration as well.
     *
     * @param content     the content object
     * @param contentType the content type
     * @return this response
     */
    default Response body(final Object content, final String contentType) {
        body(content);
        return contentType(contentType);
    }

    /**
     * Defines the request content to be sent back to the client, along with its content-type. Multipart responses may be specified using this method;
     * however, the content-type will need to specify the boundary string and the boundary will need to be specified in the
     * <code>MultipartContent</code> configuration as well.
     *
     * @param content     the content object
     * @param contentType the content type
     * @return this response
     */
    default Response body(final Object content, final ContentType contentType) {
        return body(content, contentType.getValue());
    }

    /**
     * Used to add a header to the response with the given name and value.
     *
     * @param name  the header name
     * @param value the header value
     * @return this response
     */
    Response header(String name, String... value);

    /**
     * Used to add a header to the response with the given name and value.
     *
     * @param name   the header name
     * @param values the header values
     * @return this response
     */
    Response header(String name, List<String> values);

    /**
     * Used to add multiple headers to the response.
     *
     * @param headers the headers
     * @return this response
     */
    Response headers(Map<String, Object> headers);

    /**
     * Shortcut method for adding values to the "Allow" header. These values will be appended to the existing header.
     *
     * @param methods the allowed HTTP methods
     * @return this response
     */
    default Response allows(final HttpMethod... methods) {
        return header(ALLOW, Arrays.stream(methods).map(HttpMethod::getValue).collect(toList()));
    }

    /**
     * Used to add a cookie to the response with the given name and value.
     *
     * @param name  the cookie name
     * @param value the cookie value
     * @return this response
     */
    Response cookie(String name, String value);

    /**
     * Used to add a cookie to the response.
     *
     * @param name   the cookie name
     * @param cookie the cookie definition
     * @return this response
     */
    Response cookie(String name, Cookie cookie);

    /**
     * Used to add multiple cookies to the response, with the provided names and values.
     *
     * @param cookies the cookies to be added
     * @return this response
     */
    Response cookies(Map<String, String> cookies);

    /**
     * Used to specify the content type of the response.
     *
     * @param contentType the response content type
     * @return this response
     */
    default Response contentType(final String contentType) {
        return header(CONTENT_TYPE, contentType);
    }

    /**
     * Used to specify the content type of the response.
     *
     * @param contentType the response content type
     * @return this response
     */
    default Response contentType(final ContentType contentType) {
        return contentType(contentType.getValue());
    }

    /**
     * Used to retrieve the content type of the response.
     *
     * @return the content type
     */
    String getContentType();

    /**
     * Used to specify the response code for the response.
     *
     * @param code the response code
     * @return this response
     */
    Response code(int code);

    /**
     * Used to specify a delay in the response time for the request. The response will not be returned to the client until the delay has passed.
     *
     * @param time the response delay in milliseconds
     * @return this response
     */
    Response delay(long time);

    /**
     * Used to specify a delay in the response time for the request. The response will not be returned to the client until the delay has passed.
     *
     * @param time the response delay in the specified unit
     * @param unit the time unit to use
     * @return this response
     */
    default Response delay(final long time, TimeUnit unit) {
        return delay(MILLISECONDS.convert(time, unit));
    }

    /**
     * Used to specify a delay in the response time for the request as a <code>Duration</code> string.
     *
     * @param time the delay time as a string
     * @return this response
     */
    default Response delay(final String time) {
        return delay(Duration.parse(time).toMillis());
    }

    /**
     * Used to retrieve the response delay time.
     *
     * @return the response delay time in milliseconds
     */
    long getDelay();

    /**
     * Configures the response as "chunked", with the specified chunking configuration.
     *
     * @param config the chunking configuration
     * @return a reference to this response
     */
    Response chunked(Consumer<ChunkingConfig> config);

    /**
     * Used to retrieve the configured response headers.
     *
     * @return the response headers
     */
    Map<String, List<String>> getHeaders();

    /**
     * Used to retrieve the configured response cookies. The map keys will be the cookie names, while the values will be either the cookie value as
     * as String, or a <code>Cookie</code> object.
     *
     * @return the response cookies
     */
    Map<String, Object> getCookies();

    /**
     * Used to retrieve the configured response content. The content will be converted to a byte array based on the
     * encoder configured for the content-type and content object type; if no encoder is found, the
     * <code>toString()</code> method will be called on the content object and its bytes will be used.
     * <p>
     * If no content exists, an empty array will be returned.
     *
     * @return the response content (encoded)
     */
    byte[] getContent();

    /**
     * Used to retrieve the configured response code.
     *
     * @return the response code
     */
    Integer getCode();

    /**
     * Registers a response body encoder for this response, which will override any matching encoders configured globally (or shared).
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param contentType the response content type to be encoded
     * @param objectType  the response object type to be encoded
     * @param encoder     the encoder function
     * @return a reference to this response configuration
     */
    Response encoder(String contentType, Class objectType, Function<Object, byte[]> encoder);

    /**
     * Registers a response body encoder for this response, which will override any matching encoders configured globally (or shared).
     * <p>
     * param contentType the response content-type to be encoded
     *
     * @param contentType the response content type to be encoded
     * @param objectType  the response object type to be encoded
     * @param encoder     the encoder function
     * @return a reference to this response configuration
     */
    default Response encoder(ContentType contentType, Class objectType, Function<Object, byte[]> encoder) {
        return encoder(contentType.getValue(), objectType, encoder);
    }

    /**
     * Registers the collection of shared encoders on the response. Any server-level encoders will be overridden. This
     * equivalent to configuring individual encoders on the response. Any existing encoder that matches the same
     * content-type and object type will be replaced by the incoming encoder (at the response-level).
     *
     * @param encoders the shared encoders to be applied
     * @return a reference to this response configuration
     */
    Response encoders(ResponseEncoders encoders);
}
