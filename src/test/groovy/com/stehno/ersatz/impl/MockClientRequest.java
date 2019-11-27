/*
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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.ClientRequest;
import com.stehno.ersatz.HttpMethod;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER;

public class MockClientRequest implements ClientRequest {

    private HttpMethod method;
    private String protocol;
    private String path;
    private final Map<String, Deque<String>> queryParams = new LinkedHashMap<>();
    private HeaderMap headers = new HeaderMap();
    private Map<String, Cookie> cookies = new LinkedHashMap<>();
    private byte[] body;
    private int contentLength;
    private String characterEncoding;

    public MockClientRequest() {
        // nothing
    }

    public MockClientRequest(final HttpMethod method) {
        this.method = method;
    }

    public void setCookies(Map<String, Cookie> cookies) {
        this.cookies = cookies;
    }

    public void setHeaders(HeaderMap headers) {
        this.headers = headers;
    }

    @Override public HttpMethod getMethod() {
        return method;
    }

    @Override public String getProtocol() {
        return protocol;
    }

    @Override public String getPath() {
        return path;
    }

    @Override public Map<String, Deque<String>> getQueryParams() {
        return queryParams;
    }

    @Override public HeaderMap getHeaders() {
        return headers;
    }

    @Override public Map<String, Cookie> getCookies() {
        return cookies;
    }

    @Override public byte[] getBody() {
        return body;
    }

    @Override public long getContentLength() {
        return contentLength;
    }

    @Override public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override public String getContentType() {
        return headers.getFirst(CONTENT_TYPE_HEADER);
    }

    public MockClientRequest header(final String name, final String value) {
        headers.add(new HttpString(name), value);
        return this;
    }

    public MockClientRequest query(final String name, final String... values) {
        final var params = queryParams.computeIfAbsent(name, s -> new ArrayDeque<>());

        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    params.add(value);
                }
            }
        }

        return this;
    }

    public void setContentType(String contentType) {
        header(CONTENT_TYPE_HEADER, contentType);
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public MockClientRequest cookie(String name, String value) {
        final CookieImpl cookie = new CookieImpl(name, value);
        cookies.put(name, cookie);
        return this;
    }

    public MockClientRequest cookie(String name, String value, String comment, String domain, String path, Integer maxAge, Boolean httpOnly, Boolean secure, Integer version) {
        final CookieImpl cookie = new CookieImpl(name, value);

        cookie.setComment(comment);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setVersion(version);

        cookies.put(name, cookie);

        return this;
    }
}

