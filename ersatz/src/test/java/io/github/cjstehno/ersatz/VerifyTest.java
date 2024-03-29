/**
 * Copyright (C) 2024 Christopher J. Stehno
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

import io.github.cjstehno.ersatz.cfg.Request;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith({ErsatzServerExtension.class})
public class VerifyTest {

    private final ErsatzServer server = new ErsatzServer();

    @Test void verificationFailure() {
        server.expectations(expect -> expect.GET("/alpha", Request::called));

        // never make the request - see ERROR log for details
        assertFalse(server.verify());
    }
}
