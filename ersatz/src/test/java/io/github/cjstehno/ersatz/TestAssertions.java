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
package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.ErsatzServer;
import okhttp3.Response;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public interface TestAssertions {

    static void verify(final ErsatzServer server) {
        assertTrue(server.verify());
    }

    static void assertOkWithString(final String content, final Response response) throws IOException {
        assertStatusWithString(200, content, response);
    }

    static void assertStatusWithString(final int status, final String content, final Response response) throws IOException {
        assertEquals(status, response.code());
        assertEquals(content, response.body().string());
    }

    static void assertNotFound(final Response response) {
        assertEquals(404, response.code());
    }

    static void verifyEqualityAndHashCode(final Object instanceA, final Object instanceB) {
        assertThat(instanceA, equalTo(instanceA));
        assertThat(instanceB, equalTo(instanceB));
        assertThat(instanceA, equalTo(instanceB));
        assertThat(instanceB, equalTo(instanceA));
        assertThat(instanceA, not(equalTo(null)));
        assertThat(instanceB, not(equalTo(null)));
        assertThat(instanceA.hashCode(), equalTo(instanceA.hashCode()));
        assertThat(instanceB.hashCode(), equalTo(instanceB.hashCode()));
        assertThat(instanceA.hashCode(), equalTo(instanceB.hashCode()));
        assertThat(instanceB.hashCode(), equalTo(instanceA.hashCode()));
    }
}
