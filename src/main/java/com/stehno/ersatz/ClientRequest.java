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
package com.stehno.ersatz;

import com.stehno.ersatz.cfg.HttpMethod;

import java.util.Deque;
import java.util.Map;

/**
 * An abstraction around the underlying HTTP server request that aids in matching and working with requests.
 */
public interface ClientRequest {

    /**
     * Retrieves the HTTP method for the request.
     *
     * @return the HTTP method for the request
     */
    HttpMethod getMethod();

    /**
     * Used to retrieve the request protocol, generally HTTP or HTTPS.
     *
     * @return the request protocol
     */
    String getProtocol();

    /**
     * Retrieves the request path.
     *
     * @return the request path
     */
    String getPath();

    /**
     * Retrieves the URL query string parameters for the request.
     *
     * @return the query string parameters
     */
    Map<String, Deque<String>> getQueryParams();

    /**
     * Retrieves the request headers.
     *
     * @return the request headers
     */
    Map<String, Deque<String>> getHeaders();

    /**
     * Retrieves the cookies associated with the request.
     *
     * @return the request cookies
     */
    Map<String, Cookie> getCookies();

    /**
     * Retrieves the body content (if any) as a byte array (null for an empty request).
     *
     * @return the optional body content as a byte array.
     */
    byte[] getBody();

    /**
     * Retrieves request parameters specified in the body content, if any.
     */
    Map<String, Deque<String> > getBodyParameters();

    /**
     * Retrieves the content length of the request.
     *
     * @return the request content length
     */
    long getContentLength();

    /**
     * Retrieves the request character encoding.
     *
     * @return the request character encoding
     */
    String getCharacterEncoding();

    /**
     * Retrieves the request content type. Generally this will only be present for requests with body content.
     *
     * @return the request content type
     */
    String getContentType();
}
