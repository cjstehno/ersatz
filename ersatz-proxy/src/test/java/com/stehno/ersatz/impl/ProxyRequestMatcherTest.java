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

import com.stehno.ersatz.cfg.HttpMethod;
import com.stehno.ersatz.server.ClientRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ProxyRequestMatcherTest {

    @Test @DisplayName("matcher matches")
    void matcher_matches() {
        final var matcher = new ProxyRequestMatcher(equalTo(HttpMethod.POST), endsWith("somewhere"));

        var request = Mockito.mock(ClientRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getPath()).thenReturn("/foo/bar");

        assertFalse(matcher.matches(request));
        assertEquals(0, matcher.getMatchCount());

        request = Mockito.mock(ClientRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getPath()).thenReturn("/foo/bar/somewhere");

        assertTrue(matcher.matches(request));
        assertEquals(1, matcher.getMatchCount());
    }
}
