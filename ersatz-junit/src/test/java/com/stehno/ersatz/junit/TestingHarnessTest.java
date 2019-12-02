/**
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
package com.stehno.ersatz.junit;

import com.stehno.ersatz.ErsatzServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestingHarnessTest {

    private ErsatzServer server;
    private final TestingHarness harness = new TestingHarness();

    @Test @DisplayName("finding and operating on server")
    void find_operate_server() throws Exception {
        server = mock(ErsatzServer.class);

        harness.before(this);

        verify(server, times(1)).start();

        harness.after(this);

        verify(server, times(1)).clearExpectations();
        verify(server, times(1)).close();
    }

    @Test @DisplayName("operating on null server creates instance")
    void null_server() throws Exception {
        harness.before(this);

        assertNotNull(server);

        harness.after(this); // just no exception here
    }
}