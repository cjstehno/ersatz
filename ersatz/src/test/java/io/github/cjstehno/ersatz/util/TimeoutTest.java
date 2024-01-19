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
package io.github.cjstehno.ersatz.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.cjstehno.ersatz.util.Timeout.isTrueBefore;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeoutTest {

    private AtomicBoolean state;
    private ScheduledExecutorService executor;

    @BeforeEach void beforeEach() {
        state = new AtomicBoolean(false);
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterEach void afterEach() {
        executor.shutdown();
    }

    @Test @DisplayName("when ready immediately")
    void readyImmediately() {
        changeStateIn(0);

        assertTrue(isTrueBefore(() -> state.get(), 1, SECONDS));
    }

    @Test @DisplayName("when ready before timeout")
    void readyBeforeTimeout() {
        changeStateIn(500);

        assertTrue(isTrueBefore(() -> state.get(), 1, SECONDS));
    }

    @Test @DisplayName("when timeout")
    void timeout() {
        changeStateIn(1250);

        assertFalse(isTrueBefore(() -> state.get(), 1, SECONDS));
    }

    @Test @DisplayName("when never ready")
    void neverReady() {
        assertFalse(isTrueBefore(() -> state.get(), 1, SECONDS));
    }

    private void changeStateIn(final long ms) {
        executor.schedule(() -> state.set(true), ms, MILLISECONDS);
    }
}