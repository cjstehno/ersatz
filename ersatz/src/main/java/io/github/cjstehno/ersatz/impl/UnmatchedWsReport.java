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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.WaitFor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;

/**
 * Reporter used to display mismatch information about websocket expectations.
 */
@RequiredArgsConstructor
public class UnmatchedWsReport implements Report {

    private final WebSocketExpectationsImpl expectations;
    private final AtomicReference<String> cached = new AtomicReference<>();

    @Override
    public String render() {
        if (cached.get() == null) {
            final StringBuilder out = new StringBuilder();

            out.append("# Unmatched Web Socket Message\n\n");
            out.append("# Expectations\n\n");

            out.append("Expectation (").append(expectations.getPath()).append("\n");

            out.append("  ").append(mark(expectations.isConnected())).append(" Client connection made.\n");

            final AtomicInteger failed = new AtomicInteger(0);
            expectations.eachMessage(rm -> {
                boolean matched = rm.marked(WaitFor.ONE_SECOND);
                out.append("  ").append(mark(matched)).append(" Received ").append(rm.getMessageType()).append(" message: ").append(rm.getPayload()).append("\n");
                if (!matched) {
                    failed.incrementAndGet();
                }
            });

            int count = expectations.getExpectedMessageCount() + 1;
            int matchedCount = count - failed.get();
            out.append("  (").append(count).append(" matchers: ").append(matchedCount).append(" matched, ").append((failed.get() > 0 ? RED : "") + failed).append(" failed").append(failed.get() > 0 ? RESET : "").append(")\n\n");

            cached.set(out.toString());
        }

        return cached.get();
    }

    private static String mark(final boolean ok) {
        return ok ? GREEN + "✓" + RESET : RED + "X" + RESET;
    }
}