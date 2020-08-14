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

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.server.ClientRequest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProxyExpectationsImplTest {

    private static final String PATH = "/a";
    private static final String LONG_PATH = "/alpha";
    @Mock private ClientRequest request;
    private ProxyExpectationsImpl expectations;

    @BeforeEach void beforeEach() {
        expectations = new ProxyExpectationsImpl();
    }

    @Test @DisplayName("ANY - path")
    void any_path() {
        mockRequest(HttpMethod.PUT, PATH);
        expectations.ANY(PATH);
        expectMatch();
    }

    @Test @DisplayName("ANY - matcher")
    void any_matcher() {
        mockRequest(HttpMethod.GET, LONG_PATH);
        expectations.ANY(startsWith(PATH));
        expectMatch();
    }

    @Test @DisplayName("GET - path")
    void get_path() {
        mockRequest(HttpMethod.GET, PATH);
        expectations.GET(PATH);
        expectMatch();
    }

    @Test @DisplayName("GET - matcher")
    void get_matcher() {
        mockRequest(HttpMethod.GET, LONG_PATH);
        expectations.GET(startsWith(PATH));
        expectMatch();
    }

    @Test @DisplayName("HEAD - path")
    void head_path() {
        mockRequest(HttpMethod.HEAD, PATH);
        expectations.HEAD(PATH);
        expectMatch();
    }

    @Test @DisplayName("HEAD - matcher")
    void head_matcher() {
        mockRequest(HttpMethod.HEAD, LONG_PATH);
        expectations.HEAD(startsWith(PATH));
        expectMatch();
    }

    @Test @DisplayName("PUT - path")
    void put_path() {
        mockRequest(HttpMethod.PUT, PATH);
        expectations.PUT(PATH);
        expectMatch();
    }

    @Test @DisplayName("PUT - matcher")
    void put_matcher() {
        mockRequest(HttpMethod.PUT, LONG_PATH);
        expectations.PUT(startsWith(PATH));
        expectMatch();
    }

    @Test @DisplayName("POST - path")
    void post_path() {
        mockRequest(HttpMethod.POST, PATH);
        expectations.POST(PATH);
        expectMatch();
    }

    @Test @DisplayName("POST - matcher")
    void post_matcher() {
        mockRequest(HttpMethod.POST, LONG_PATH);
        expectations.POST(startsWith(PATH));
        expectMatch();
    }

    @Test @DisplayName("DELETE - path")
    void delete_path() {
        mockRequest(HttpMethod.DELETE, PATH);
        expectations.DELETE(PATH);
        expectMatch();
    }

    @Test @DisplayName("DELETE - matcher")
    void delete_matcher() {
        mockRequest(HttpMethod.DELETE, LONG_PATH);
        expectations.DELETE(CoreMatchers.startsWith(PATH));
        expectMatch();
    }

    @Test @DisplayName("PATCH - path")
    void patch_path() {
        mockRequest(HttpMethod.PATCH, PATH);
        expectations.PATCH(PATH);
        expectMatch();
    }

    @Test @DisplayName("PATCH - matcher")
    void patch_matcher() {
        mockRequest(HttpMethod.PATCH, LONG_PATH);
        expectations.PATCH(CoreMatchers.startsWith(PATH));
        expectMatch();
    }

    @Test @DisplayName("OPTIONS - path")
    void options_path() {
        mockRequest(HttpMethod.OPTIONS, PATH);
        expectations.OPTIONS(PATH);
        expectMatch();
    }

    @Test @DisplayName("OPTIONS - matcher")
    void options_matcher() {
        mockRequest(HttpMethod.OPTIONS, LONG_PATH);
        expectations.OPTIONS(CoreMatchers.startsWith(PATH));
        expectMatch();
    }

    private void mockRequest(final HttpMethod put, final String path) {
        when(request.getMethod()).thenReturn(put);
        when(request.getPath()).thenReturn(path);
    }

    private void expectMatch() {
        assertTrue(expectations.getMatchers().get(0).matches(request));
    }
}
