package com.stehno.ersatz;

import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;

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
    // FIXME: this is server-specific
    HeaderMap getHeaders();

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
