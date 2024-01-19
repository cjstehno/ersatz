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
package io.github.cjstehno.ersatz.cfg;

import org.junit.jupiter.api.Test;

import static io.github.cjstehno.ersatz.cfg.WaitFor.FOREVER;
import static io.github.cjstehno.ersatz.cfg.WaitFor.ONE_SECOND;
import static io.github.cjstehno.ersatz.cfg.WaitFor.atMost;
import static io.github.cjstehno.testthings.Verifiers.verifyEqualsAndHashCode;
import static io.github.cjstehno.testthings.Verifiers.verifyToString;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WaitForTest {

    @Test void usage() {
        var waitFor = atMost(2, MINUTES);
        assertEquals(2, waitFor.getTime());
        assertEquals(MINUTES, waitFor.getUnit());

        waitFor = atMost(3);
        assertEquals(3, waitFor.getTime());
        assertEquals(SECONDS, waitFor.getUnit());

        waitFor = ONE_SECOND;
        assertEquals(1, waitFor.getTime());
        assertEquals(SECONDS, waitFor.getUnit());

        waitFor = FOREVER;
        assertEquals(Long.MAX_VALUE, waitFor.getTime());
        assertEquals(HOURS, waitFor.getUnit());
    }

    @Test void equalsAndHash() {
        verifyEqualsAndHashCode(
            atMost(3, SECONDS),
            atMost(3, SECONDS)
        );
    }

    @Test void string(){
        verifyToString(
            "WaitFor(time=1, unit=SECONDS)",
            ONE_SECOND
        );
    }
}