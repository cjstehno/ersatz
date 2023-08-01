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
package io.github.cjstehno.ersatz.util;

import static io.github.cjstehno.ersatz.util.StatusCode.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StatusCodeTest {

    @Test void ofValue(){
        assertEquals(OK, StatusCode.of(200));
        assertEquals(LOOP_DETECTED, StatusCode.of(508));
        assertEquals(NOT_FOUND, StatusCode.of(404));
    }

    @Test void informational(){
        assertTrue(isInformational(CONTINUE));
        assertFalse(isInformational(OK));
    }

    @Test void successful(){
        assertTrue(isSuccessful(OK));
        assertFalse(isSuccessful(NOT_FOUND));
    }

    @Test void redirection(){
        assertTrue(isRedirection(PERMANENT_REDIRECT));
        assertFalse(isRedirection(INTERNAL_SERVER_ERROR));
    }

    @Test void clientError(){
        assertTrue(isClientError(NOT_FOUND));
        assertFalse(isClientError(INTERNAL_SERVER_ERROR));
    }

    @Test void serverError(){
        assertTrue(isServerError(INTERNAL_SERVER_ERROR));
        assertFalse(isServerError(NOT_FOUND));
    }
}