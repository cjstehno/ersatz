/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import lombok.val;
import org.apache.commons.fileupload.portlet.PortletRequestContext;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.*;
import static io.github.cjstehno.ersatz.match.HttpMethodMatcher.methodMatching;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class HttpMethodMatcherTest {

    @ParameterizedTest @CsvSource({
        "ANY,GET,true",
        "ANY,HEAD,true",
        "ANY,POST,true",
        "ANY,PUT,true",
        "ANY,PATCH,true",
        "ANY,OPTIONS,true",
        "ANY,DELETE,true",
        "ANY,TRACE,true",
        "GET,GET,true",
        "GET,PUT,false",
        "POST,POST,true",
        "PATCH,OPTIONS,false"
    })
    void methodMatch(final HttpMethod required, final HttpMethod requested, final boolean matched) {
        assertEquals(matched, methodMatching(required).matches(new MockClientRequest(requested, "/testing")));
    }

    @Test @DisplayName("method match with matcher")
    void matchWithMatcher(){
        val request = new MockClientRequest(GET, "/test");
        assertTrue(methodMatching(equalTo(GET)).matches(request));
        assertFalse(methodMatching(equalTo(POST)).matches(request));
    }

    @Test @DisplayName("matching multiple methods")
    void matchMultipleMethods(){
        val request = new MockClientRequest(POST, "/test");
        assertTrue(methodMatching(ANY).matches(request));
        assertTrue(methodMatching(GET, POST).matches(request));
        assertFalse(methodMatching(PUT, DELETE).matches(request));
    }
}