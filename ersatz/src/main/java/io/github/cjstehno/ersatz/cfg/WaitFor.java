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
package io.github.cjstehno.ersatz.cfg;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

/**
 * An abstraction for expectation timeouts, allowing for explicit values, along with a FOREVER value.
 * <p>
 * Generally, it is advisable to minimize your waiting time to avoid allowing a failing test to over-extend the build
 * runtime, though there are cases where tests run slow on certain machines, so it is good to find the best value for
 * your configurations.
 */
@RequiredArgsConstructor(access = PRIVATE) @Getter @EqualsAndHashCode @ToString
public class WaitFor {

    /**
     * Denotes that the waiting should continue until the conditions are met - never timing out.
     *
     * The actual values are configured to <code>Long.MAX_VALUE HOURS</code>, though they may not necessarily be used.
     */
    public static final WaitFor FOREVER = new WaitFor(Long.MAX_VALUE, HOURS);

    /**
     * Denotes that the waiting should continue for 1 second, if the conditions are not met, then timeout.
     */
    public static final WaitFor ONE_SECOND = new WaitFor(1, SECONDS);

    private final long time;
    private final TimeUnit unit;

    /**
     * Denotes that the waiting should continue until the elapsed time (with unit), if the conditions are not met, and
     * then it should timeout.
     *
     * @param time the waiting time (in units)
     * @param unit the units of the waiting time
     * @return the configured WaitFor instance
     */
    public static WaitFor atMost(final long time, final TimeUnit unit) {
        return new WaitFor(time, unit);
    }

    /**
     * Denotes that the waiting should continue until the elapsed time (in seconds), if the conditions are not met, and
     * then it should timeout.
     *
     * @param seconds the waiting time (in seconds)
     * @return the configured WaitFor instance
     */
    public static WaitFor atMost(final long seconds) {
        return atMost(seconds, SECONDS);
    }
}
