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

import io.github.cjstehno.ersatz.server.MockClientRequest;
import org.junit.jupiter.api.Test;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyServerExpectationsImplTest {

    @Test void anyExpectation() {
        assertTrue(
            ((ProxyServerExpectationsImpl) new ProxyServerExpectationsImpl().any("/alpha")).matches(
                new MockClientRequest(GET, "/alpha")
            )
        );
        assertFalse(
            ((ProxyServerExpectationsImpl) new ProxyServerExpectationsImpl().any("/alpha")).matches(
                new MockClientRequest(GET, "/bravo")
            )
        );
    }
}