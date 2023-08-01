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
package io.github.cjstehno.ersatz.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.cjstehno.ersatz.cfg.HttpMethod;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProxyServerConfigImplTest {

    @Test void properties() throws URISyntaxException {
        val config = (ProxyServerConfigImpl) new ProxyServerConfigImpl()
            .autoStart(false)
            .target("http://localhost:1234/foo")
            .expectations(expect -> expect.get("/bar"));

        assertFalse( config.isAutoStart());
        assertEquals(new URI("http://localhost:1234/foo"), config.getTargetUri());

        val request = new MockClientRequest(HttpMethod.GET, "/bar");
        assertTrue(config.getExpectations().matches(request));
    }
}