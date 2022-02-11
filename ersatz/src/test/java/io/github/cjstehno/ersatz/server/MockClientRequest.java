/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.server;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.encdec.Cookie;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.cjstehno.ersatz.cfg.ContentType.CONTENT_TYPE_HEADER;

public class MockClientRequest implements ClientRequest {

    private HttpMethod method;
    private String scheme;
    private String path;
    private final Map<String, Deque<String>> queryParams = new LinkedHashMap<>();
    private final Map<String, Deque<String>> headers = new LinkedHashMap<>();
    private final Map<String, Deque<String>> bodyParameters = new LinkedHashMap<>();
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

    public MockClientRequest(final HttpMethod method, final String path) {
        this(method);
        setPath(path);
    }

    public MockClientRequest(final byte[] content) {
        setBody(content);
    }

    public void setCookies(Map<String, Cookie> cookies) {
        this.cookies = cookies;
    }

    public void setHeaders(Map<String, Deque<String>> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
    }

    @Override public HttpMethod getMethod() {
        return method;
    }

    @Override public String getScheme() {
        return scheme;
    }

    @Override public String getPath() {
        return path;
    }

    @Override public Map<String, Deque<String>> getQueryParams() {
        return queryParams;
    }

    @Override public Map<String, Deque<String>> getHeaders() {
        return headers;
    }

    @Override public Map<String, Cookie> getCookies() {
        return cookies;
    }

    @Override public byte[] getBody() {
        return body;
    }

    @Override public Map<String, Deque<String>> getBodyParameters() {
        return bodyParameters;
    }

    public void setBodyParameters(final Map<String, Deque<String>> params) {
        bodyParameters.clear();
        bodyParameters.putAll(params);
    }

    @Override public long getContentLength() {
        return contentLength;
    }

    @Override public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override public String getContentType() {
        return headers.containsKey(CONTENT_TYPE_HEADER) ? headers.get(CONTENT_TYPE_HEADER).getFirst() : null;
    }

    public MockClientRequest header(final String name, final String value) {
        headers.computeIfAbsent(name, s -> new ArrayDeque<>()).add(value);
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

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public MockClientRequest scheme(final String scheme) {
        this.scheme = scheme;
        return this;
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
        return cookie(name, value, null, null, null, 0, false, false, 0);
    }

    public MockClientRequest cookie(final String name, String value, String comment, String domain, String path, Integer maxAge, Boolean httpOnly, Boolean secure, Integer version) {
        cookies.put(name, new Cookie(value, comment, domain, path, version, httpOnly, maxAge, secure));
        return this;
    }
}

