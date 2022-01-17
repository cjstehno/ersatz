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
package com.stehno.ersatz;

import okhttp3.Response;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface TestAssertions {

    static void verify(final ErsatzServer server) {
        assertTrue(server.verify());
    }

    static void assertOkWithString(final String content, final Response response) throws IOException {
        assertEquals(200, response.code());
        assertEquals(content, response.body().string());
    }

    static void assertNotFound(final Response response) {
        assertEquals(404, response.code());
    }
}
