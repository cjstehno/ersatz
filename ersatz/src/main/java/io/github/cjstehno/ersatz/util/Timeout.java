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

import lombok.NoArgsConstructor;
import lombok.val;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

/**
 * A utility for operation timeout support.
 */
@NoArgsConstructor(access = PRIVATE)
public final class Timeout {

    /**
     * Used to wrap a blocking timeout around the provided condition. If it is not met (resolves <code>true</code>)
     * before the timeout expires, a value of <code>false</code> will be returned.
     *
     * @param condition the boolean condition waiting to be resolved as true
     * @param timeout the timeout time value
     * @param unit the timeout units
     * @return a value of true if the condition was met before the timeout expired
     */
    public static boolean isTrueBefore(final Supplier<Boolean> condition, final long timeout, final TimeUnit unit) {
        val started = currentTimeMillis();
        val timeoutMs = unit.toMillis(timeout);

        if (!condition.get()) {
            val executor = newSingleThreadScheduledExecutor();
            try {
                var future = executor.schedule(condition::get, 250, MILLISECONDS);

                while (!future.get(timeout, unit) && !timedOut(started, timeoutMs)) {
                    future = executor.schedule(condition::get, 250, MILLISECONDS);
                }

                return !timedOut(started, timeoutMs);

            } catch (final Exception e) {
                return false;
            } finally {
                executor.shutdown();
            }

        } else {
            return true;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean timedOut(final long started, final long timeoutMs) {
        return currentTimeMillis() - started >= timeoutMs;
    }
}
