/*
 * Copyright (C) 2018 Christopher J. Stehno
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
package com.stehno.ersatz.impl

import groovy.transform.Memoized
import groovy.transform.TupleConstructor

import java.util.concurrent.TimeUnit

@TupleConstructor
class UnmatchedWsReport {

    final WebSocketExpectationsImpl expectations

    private static final String RED = '\u001b[31m'
    private static final String GREEN = '\u001b[32m'
    private static final String RESET = '\u001b[0m'

    @Override @Memoized(maxCacheSize = 1, protectedCacheSize = 1) String toString() {
        StringBuilder out = new StringBuilder()

        out.append('# Unmatched Web Socket Message\n\n')

        out.append('# Expectations\n\n')

        out.append "Expectation (${expectations.path}):\n"

        out.append("  ${mark(expectations.connected)} Client connection made.\n")

        int failed = 0
        expectations.eachMessage { ReceivedMessageImpl rm ->
            boolean matched = rm.marked(1, TimeUnit.SECONDS)
            out.append("  ${mark(matched)} Received ${rm.messageType} message: ${rm.payload}\n")
            if (!matched) {
                failed++
            }
        }

        int count = expectations.expectedMessageCount + 1
        out.append("  ($count matchers: ${count - failed} matched, ${failed ? RED : ''}$failed failed${failed ? RESET : ''})\n\n")

        out.toString()
    }

    private static String mark(final boolean ok) {
        ok ? "${GREEN}✓${RESET}" : "${RED}X${RESET}"
    }
}